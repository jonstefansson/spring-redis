package net.jonstef.spring.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.jonstef.redis.EventProcessingKeyListener;
import net.jonstef.redis.KeyListener;
import net.jonstef.redis.RedisPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * @author Jon Stefansson
 */
@Configuration
public class ApplicationConfig {

	@Value("#{dw.executorConfiguration.corePoolSize}")
	private int corePoolSize;

	@Value("#{dw.executorConfiguration.maxPoolSize}")
	private int maxPoolSize;

	@Value("#{dw.redisConfiguration.usePool}")
	private boolean usePool;

	@Value("#{dw.redisConfiguration.host}")
	private String host;

	@Value("#{dw.redisConfiguration.port}")
	private int port;

	@Value("#{dw.redisConfiguration.password}")
	private String password;

	@Value("#{dw.redisConfiguration.database}")
	private int database;

	@Value("#{dw.redisConfiguration.timeout}")
	private int timeout;

	@Bean
	JedisConnectionFactory connectionFactory() {
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setUsePool(usePool);
		jedisConnectionFactory.setHostName(host);
		jedisConnectionFactory.setPort(port);
		jedisConnectionFactory.setPassword(password);
		jedisConnectionFactory.setDatabase(database);
		jedisConnectionFactory.setTimeout(timeout);
		return jedisConnectionFactory;
	}

	@Bean
	Executor taskExecutor() throws Exception {
		ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
		taskExecutor.setThreadFactory(new ThreadFactoryBuilder().setNameFormat("Task-%d").build());
		taskExecutor.setCorePoolSize(corePoolSize);
		taskExecutor.setMaxPoolSize(maxPoolSize);
		taskExecutor.setAllowCoreThreadTimeOut(true);
		taskExecutor.prefersShortLivedTasks();
		return taskExecutor;
	}

	@Bean
	Executor pollingExecutor() {
		return Executors.newSingleThreadExecutor(new ThreadFactoryBuilder().setNameFormat("Poller-%d").build());
	}

	@Bean
	StringRedisTemplate redisTemplate(JedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

}
