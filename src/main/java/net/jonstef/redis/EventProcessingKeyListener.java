package net.jonstef.redis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static net.jonstef.redis.Keys.*;

/**
 * @author Jon Stefansson
 */
@Component
public class EventProcessingKeyListener implements KeyListener {

	private final StringRedisTemplate redisTemplate;

	private final Logger logger = LoggerFactory.getLogger(EventProcessingKeyListener.class);

	@Autowired
	public EventProcessingKeyListener(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@Override
	public void process(final String key) {
		logger.info("process(key={})", key);
		Map<Object, Object> eventData = redisTemplate.boundHashOps(key).entries();
		List<Object> results = redisTemplate.executePipelined(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				List<Object> results;
				if (isSuccess(key)) {
					operations.multi();
					operations.opsForList().remove(EVENT_PROCESSING, 0, key);
					operations.opsForList().rightPush(EVENT_SUCCESS, key);
					operations.expire(key, 24, TimeUnit.HOURS);
					results = operations.exec();
				}
				else {
					operations.multi();
					operations.opsForList().remove(EVENT_PROCESSING, 0, key);
					operations.opsForList().rightPush(EVENT_FAILED, key);
					operations.expire(key, 24, TimeUnit.HOURS);
					results = operations.exec();
				}
				return results;
			}
		});
		logger.info("results = {}", results);
	}

	private boolean isSuccess(String key) {
		UUID uuid = UUID.fromString(key.substring(6));
		boolean success = (uuid.getLeastSignificantBits() % 2) == 0;
		logger.info("isSuccess(key={}) {}", key, success);
		return success;
	}

}
