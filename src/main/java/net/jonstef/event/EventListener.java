package net.jonstef.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Jon Stefansson
 */
public class EventListener {

	private final Logger logger = LoggerFactory.getLogger(EventListener.class);

	public void receiveMessage(String message) {
		logger.info("received message: {}", message);
	}

}
