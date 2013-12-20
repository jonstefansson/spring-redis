package net.jonstef.service;

import com.github.nhuray.dropwizard.spring.SpringBundle;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import net.jonstef.configuration.SpringRedisConfiguration;
import net.jonstef.healthcheck.RedisHealthCheck;
import net.jonstef.resource.EventResource;
import net.jonstef.spring.ContextFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
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
		ConfigurableApplicationContext context = new ContextFactory().buildContext();
		bootstrap.addBundle(new SpringBundle(context, true, true, true));
	}

	@Override
	public void run(SpringRedisConfiguration configuration, Environment environment) throws Exception {
		// SpringBundle takes care of assembling the Dropwizard app
	}

	public static void main(String[] args) throws Exception {
		new SpringRedisService().run(args);
	}

}
