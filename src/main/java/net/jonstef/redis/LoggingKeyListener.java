package net.jonstef.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Stefansson
 */
public class LoggingKeyListener implements KeyListener {

	private final Logger logger = LoggerFactory.getLogger(LoggingKeyListener.class);

	@Override
	public void process(final String key) {
		logger.info("[{}] processing key: {}", Thread.currentThread().getName(), key);
	}

}
