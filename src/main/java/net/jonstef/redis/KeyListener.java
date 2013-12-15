package net.jonstef.redis;

/**
 * @author Jon Stefansson
 */
public interface KeyListener {

	void process(String key);

}
