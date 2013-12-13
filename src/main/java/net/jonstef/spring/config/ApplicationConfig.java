package net.jonstef.spring.config;

import net.jonstef.configuration.RedisConfiguration;
import net.jonstef.configuration.SpringRedisConfiguration;
import net.jonstef.event.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

import java.util.concurrent.Executor;

/**
 * @author Jon Stefansson
 */
@Configuration
public class ApplicationConfig {

	@Autowired
	private SpringRedisConfiguration configuration;

	@Autowired
	private Executor managedExecutor;

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
	RedisMessageListenerContainer container(final JedisConnectionFactory connectionFactory) {
		RedisMessageListenerContainer container = new RedisMessageListenerContainer() {{
			setConnectionFactory(connectionFactory);
			setTaskExecutor(managedExecutor);
		}};
		container.addMessageListener(listenerAdapter(), new PatternTopic("events"));
		return container;
	}

	@Bean
	MessageListenerAdapter listenerAdapter() {
		return new MessageListenerAdapter(new EventListener(), "receiveMessage");
	}

	@Bean
	StringRedisTemplate template(JedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

}
