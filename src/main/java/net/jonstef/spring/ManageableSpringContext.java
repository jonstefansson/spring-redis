package net.jonstef.spring;

import com.yammer.dropwizard.lifecycle.Managed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

/**
 * @author Jon Stefansson
 */
public class ManageableSpringContext implements Managed {

	private final Lifecycle context;
	private final Logger logger = LoggerFactory.getLogger(ManageableSpringContext.class);

	public ManageableSpringContext(Lifecycle context) {
		this.context = context;
	}

	@Override
	public void start() throws Exception {
		if (context.isRunning()) {
			context.start();
			logger.info("Spring context started");
		}
	}

	@Override
	public void stop() throws Exception {
		logger.info("stop() stopping Spring application context");
		context.stop();
	}

}
