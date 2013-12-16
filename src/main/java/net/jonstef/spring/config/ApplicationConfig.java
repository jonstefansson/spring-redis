package net.jonstef.spring.config;

import net.jonstef.configuration.RedisConfiguration;
import net.jonstef.configuration.SpringRedisConfiguration;
import net.jonstef.redis.EventProcessingKeyListener;
import net.jonstef.redis.KeyListener;
import net.jonstef.redis.RedisPoller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.Executor;

import static net.jonstef.redis.Keys.EVENT_PROCESSING;
import static net.jonstef.redis.Keys.EVENT_QUEUE;

/**
 * @author Jon Stefansson
 */
@Configuration
public class ApplicationConfig {

	@Autowired
	private SpringRedisConfiguration configuration;

	@Autowired
	private Executor taskExecutor;

	@Autowired
	private Executor pollingExecutor;

	@Bean
	JedisConnectionFactory connectionFactory() {
		RedisConfiguration c = configuration.getRedisConfiguration();
		JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory();
		jedisConnectionFactory.setUsePool(c.getUsePool());
		jedisConnectionFactory.setHostName(c.getHost());
		jedisConnectionFactory.setPort(c.getPort());
		jedisConnectionFactory.setPassword(c.getPassword());
		jedisConnectionFactory.setDatabase(c.getDatabase());
		jedisConnectionFactory.setTimeout(c.getTimeout());
		return jedisConnectionFactory;
	}

	@Bean
	KeyListener keyListener() {
		return new EventProcessingKeyListener(redisTemplate(connectionFactory()));
	}

	@Bean
	RedisPoller poller() {
		RedisPoller redisPoller = new RedisPoller() {{
			setStringRedisTemplate(redisTemplate(connectionFactory()));
			setSourceKey(EVENT_QUEUE);
			setDestinationKey(EVENT_PROCESSING);
			setKeyListener(keyListener());
			setTaskExecutor(taskExecutor);
			setPollingExecutor(pollingExecutor);
		}};
		return redisPoller;
	}

	@Bean
	StringRedisTemplate redisTemplate(JedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

}
