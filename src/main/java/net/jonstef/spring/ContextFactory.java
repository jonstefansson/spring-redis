package net.jonstef.spring;

import com.yammer.dropwizard.config.Environment;
import net.jonstef.configuration.ExecutorConfiguration;
import net.jonstef.configuration.SpringRedisConfiguration;
import net.jonstef.spring.config.ApplicationConfig;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Jon Stefansson
 */
public class ContextFactory {

	public GenericApplicationContext buildContext(SpringRedisConfiguration configuration, Environment environment) {

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.getBeanFactory().registerSingleton("configuration", configuration);

		ExecutorConfiguration executorConfiguration = configuration.getExecutorConfiguration();
		ExecutorService pollerExecutorService = environment.managedExecutorService(
				"Poller-%d",
				1,
				2,
				executorConfiguration.getKeepAliveTime(),
				TimeUnit.MILLISECONDS);
		context.getBeanFactory().registerSingleton("pollingExecutor", pollerExecutorService);

		ExecutorService taskExecutorService = environment.managedExecutorService(
				"Tasks-%d",
				executorConfiguration.getCorePoolSize(),
				executorConfiguration.getMaximumPoolSize(),
				executorConfiguration.getKeepAliveTime(),
				TimeUnit.MILLISECONDS);
		context.getBeanFactory().registerSingleton("taskExecutor", taskExecutorService);

		context.register(ApplicationConfig.class);

		context.refresh();
		return context;
	}

}
