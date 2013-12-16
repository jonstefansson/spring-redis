package net.jonstef.redis;

/**
 * @author Jon Stefansson
 */
public interface Keys {

	public static final String EVENT_QUEUE = "list:event:queue";
	public static final String EVENT_PROCESSING = "list:event:processing";
	public static final String EVENT_SUCCESS = "list:event:success";
	public static final String EVENT_FAILED = "list:event:failed";

}
