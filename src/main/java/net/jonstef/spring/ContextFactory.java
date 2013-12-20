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

	public GenericApplicationContext buildContext() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
		context.scan("net.jonstef");
		return context;
	}

}
