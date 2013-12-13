package net.jonstef.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Jon Stefansson
 */
public class ExecutorConfiguration extends Configuration {

	@Min(1)
	@Max(128)
	@JsonProperty
	private int corePoolSize;

	@Min(1)
	@Max(256)
	@JsonProperty
	private int maximumPoolSize;

	@Min(1)
	@Max(65535)
	@JsonProperty
	private long keepAliveTime;

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}
}
