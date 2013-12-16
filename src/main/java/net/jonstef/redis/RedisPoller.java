package net.jonstef.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.SchedulingAwareRunnable;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static net.jonstef.redis.Keys.*;

/**
 * @author Jon Stefansson
 */
public class RedisPoller implements InitializingBean, DisposableBean, BeanNameAware, SmartLifecycle {

	/**
	 * The default recovery interval: 5000 ms = 5 seconds.
	 */
	public static final long DEFAULT_RECOVERY_INTERVAL = 5000;

	private final Logger logger = LoggerFactory.getLogger(RedisPoller.class);

	private String beanName;

	private StringRedisTemplate stringRedisTemplate;

	private String sourceKey;
	private String destinationKey;
	private KeyListener keyListener;

	// long-running threads that block until an event is popped
	private Executor pollingExecutor;
	// threads for processing events after they have been popped
	private Executor taskExecutor;

	private long initWait = TimeUnit.SECONDS.toMillis(5);

	private final PollerTask pollerTask = new PollerTask();

	private final Object monitor = new Object();
	// whether the container is running (or not)
	private volatile boolean running = false;
	// whether the poller has been initialized
	private volatile boolean initialized = false;
	private volatile boolean manageExecutor = false;
	// whether the container uses a connection or not
	// (as the container might be running but w/o listeners, it won't use any resources)
	private volatile boolean listening = false;

	private long recoveryInterval = DEFAULT_RECOVERY_INTERVAL;

	public void setStringRedisTemplate(StringRedisTemplate stringRedisTemplate) {
		this.stringRedisTemplate = stringRedisTemplate;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}

	public void setDestinationKey(String destinationKey) {
		this.destinationKey = destinationKey;
	}

	public void setKeyListener(KeyListener keyListener) {
		this.keyListener = keyListener;
	}

	/**
	 * Creates a default TaskExecutor. Called if no explicit TaskExecutor has been specified.
	 * <p>The default implementation builds a {@link org.springframework.core.task.SimpleAsyncTaskExecutor}
	 * with the specified bean name (or the class name, if no bean name specified) as thread name prefix.
	 * @see org.springframework.core.task.SimpleAsyncTaskExecutor#SimpleAsyncTaskExecutor(String)
	 */
	protected TaskExecutor createDefaultTaskExecutor() {
		return new SimpleAsyncTaskExecutor("TaskExecutor-");
	}

	protected TaskExecutor createDefaultPollingExecutor() {
		return new SimpleAsyncTaskExecutor("PollingExecutor-");
	}

	/**
	 * Sets the task execution used for polling Redis channels. By default, if no executor is set,
	 * the {@link #setTaskExecutor(Executor)} will be used. In some cases, this might be undesired as
	 * the listening to the connection is a long running task.
	 *
	 * <p/>Note: This implementation uses at most one long running thread (depending on whether there are any listeners registered or not)
	 * and up to two threads during the initial registration.
	 *
	 * @param pollingExecutor The pollingExecutor to set.
	 */
	public void setPollingExecutor(Executor pollingExecutor) {
		this.pollingExecutor = pollingExecutor;
	}

	/**
	 * Sets the task executor used for running the message listeners when messages are received.
	 * If no task executor is set, an instance of {@link SimpleAsyncTaskExecutor} will be used by default.
	 * The task executor can be adjusted depending on the work done by the listeners and the number of
	 * messages coming in.
	 *
	 * @param taskExecutor The taskExecutor to set.
	 */
	public void setTaskExecutor(Executor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	@Override
	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void destroy() throws Exception {
		initialized = false;

		stop();

		if (manageExecutor) {
			if (taskExecutor instanceof DisposableBean) {
				((DisposableBean) taskExecutor).destroy();

				if (logger.isDebugEnabled()) {
					logger.debug("Stopped internally-managed task executor");
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (taskExecutor == null) {
			manageExecutor = true;
			taskExecutor = createDefaultTaskExecutor();
		}

		if (pollingExecutor == null) {
			pollingExecutor = createDefaultPollingExecutor();
		}

		initialized = true;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public void start() {
		if (!running) {
			running = true;
			// wait for the poller to start before returning
			synchronized (monitor) {
				lazyListen();
				if(listening) {
					try {
						// wait up to 5 seconds for poller thread
						monitor.wait(initWait);
					} catch (InterruptedException e) {
						// stop waiting
					}
				}
			}

			if (logger.isDebugEnabled()) {
				logger.debug("Started RedisPoller");
			}
		}
	}

	@Override
	public void stop() {
		if (isRunning()) {
			running = false;
			pollerTask.cancel();
		}

		if (logger.isDebugEnabled()) {
			logger.debug("Stopped RedisPoller");
		}

	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Method inspecting whether listening for events (and thus using a thread) is actually needed and triggering it.
	 */
	private void lazyListen() {
		if (isRunning()) {
			if (!listening) {
				synchronized (monitor) {
					if (!listening) {
						pollingExecutor.execute(pollerTask);
						listening = true;
						logger.debug("Started polling for Redis events");
					}
				}
			}
		}
	}

	/**
	 * Runnable used for Redis subscription. Adapted from the SubscriptionTask in RedisMessageListenerContainer.
	 *
	 */
	private class PollerTask implements SchedulingAwareRunnable {

		private volatile boolean running = true;

		public boolean isLongLived() {
			return true;
		}

		public void run() {
			try {
				while(running) {
					final String key = stringRedisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey, 0, TimeUnit.SECONDS);
					logger.info("polled key={}", key);
					processKey(key);
				}
			}
			catch (Throwable t) {
				handlePollerException(t);
			}
		}

		void cancel() {
			running = false;
		}

	}

	protected void processKey(final String key) {
		if (key != null) {
			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						keyListener.process(key);
					}
					catch (Throwable t) {
						logger.error(String.format("Failed to process event with key=%s", key), t);
					}
				}
			});
		}
	}

	/**
	 * Handle poller task exception. Will attempt to restart the poller
	 * if the Exception is a connection failure (for example, Redis was restarted).
	 * @param ex Throwable exception
	 */
	protected void handlePollerException(Throwable ex) {
		logger.warn("handlePollerException: {}", ex.toString());
		listening = false;
		if(ex instanceof RedisConnectionFailureException) {
			if(isRunning()) {
				logger.error(String.format("Connection failure occurred. Restarting poller task after %d ms", recoveryInterval));
				sleepBeforeRecoveryAttempt();
				lazyListen();
			}
		} else {
			logger.error("PollerTask aborted with exception:", ex);
		}
	}

	/**
	 * Sleep according to the specified recovery interval.
	 * Called between recovery attempts.
	 */
	protected void sleepBeforeRecoveryAttempt() {
		if (this.recoveryInterval > 0) {
			try {
				Thread.sleep(this.recoveryInterval);
			}
			catch (InterruptedException interEx) {
				logger.debug("Thread interrupted while sleeping the recovery interval");
			}
		}
	}

}
