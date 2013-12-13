package net.jonstef.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * @author Jon Stefansson
 */
public class RedisConfiguration {

	@JsonProperty
	@NotEmpty
	private String host;

	@Min(1)
	@Max(65535)
	@JsonProperty
	private int port = 6379;

	@Min(1)
	@Max(65535)
	@JsonProperty
	private int timeout = 2000;

	@JsonProperty
	private String password;

	@Min(0)
	@Max(15)
	private int database = 0;

	@JsonProperty
	private boolean usePool = false;

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getPassword() {
		return password;
	}

	public int getDatabase() {
		return database;
	}

	public boolean getUsePool() {
		return usePool;
	}

}
