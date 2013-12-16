package net.jonstef.service;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import net.jonstef.configuration.SpringRedisConfiguration;
import net.jonstef.healthcheck.RedisHealthCheck;
import net.jonstef.resource.EventResource;
import net.jonstef.spring.ContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * @author Jon Stefansson
 */
public class SpringRedisService extends Service<SpringRedisConfiguration> {

	private final Logger logger = LoggerFactory.getLogger(SpringRedisService.class);

	@Override
	public void initialize(Bootstrap<SpringRedisConfiguration> bootstrap) {
		bootstrap.setName("spring-redis");
	}

	@Override
	public void run(SpringRedisConfiguration configuration, Environment environment) throws Exception {

		GenericApplicationContext context = new ContextFactory().buildContext(configuration, environment);
		context.registerShutdownHook();

		StringRedisTemplate template = context.getBean(StringRedisTemplate.class);
		EventResource eventResource = new EventResource(template);
		environment.addResource(eventResource);

		environment.addHealthCheck(new RedisHealthCheck(template));

	}

	public static void main(String[] args) throws Exception {
		new SpringRedisService().run(args);
	}

}
