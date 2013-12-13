package net.jonstef.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * You can override configuration settings with Java System properties. Be sure to prefix the property with dw.*
 *
 * <pre>java -Ddw.http.port=9090 server configuration.yaml</pre>
 *
 * @author Jon Stefansson
 *
 */
public class SpringRedisConfiguration extends Configuration {

	@Valid
	@NotNull
	@JsonProperty("redis")
	private RedisConfiguration redisConfiguration = new RedisConfiguration();

	@Valid
	@NotNull
	@JsonProperty("executor")
	private ExecutorConfiguration executorConfiguration = new ExecutorConfiguration();

	public RedisConfiguration getRedisConfiguration() {
		return redisConfiguration;
	}

	public ExecutorConfiguration getExecutorConfiguration() {
		return executorConfiguration;
	}

}
