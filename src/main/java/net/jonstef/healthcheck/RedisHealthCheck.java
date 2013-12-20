package net.jonstef.healthcheck;

import com.yammer.metrics.core.HealthCheck;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @author Jon Stefansson
 */
@Component
public class RedisHealthCheck extends HealthCheck {

	private final StringRedisTemplate template;

	@Autowired
	public RedisHealthCheck(StringRedisTemplate template) {
		super("redis");
		this.template = template;
	}

	@Override
	protected Result check() throws Exception {
		try {
			String pong = template.execute(new RedisCallback<String>() {
				@Override
				public String doInRedis(RedisConnection connection) throws DataAccessException {
					return connection.ping();
				}
			});
			if ("PONG".equals(pong)) {
				return Result.healthy();
			}
			else {
				return Result.unhealthy(String.format("Unexpected response from Redis: %s", pong));
			}
		}
		catch (Exception e) {
			return Result.unhealthy(e);
		}
	}

}
