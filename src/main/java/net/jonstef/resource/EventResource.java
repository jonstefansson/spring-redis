package net.jonstef.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.UUID;

import static net.jonstef.redis.Keys.*;

/**
 * @author Jon Stefansson
 */
@Path("/event")
@Component
public class EventResource {

	private final StringRedisTemplate redisTemplate;

	private final Logger logger = LoggerFactory.getLogger(EventResource.class);

	@Autowired
	public EventResource(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@POST
	public Response sendEvent(@FormParam("message") final String eventMessage) {
		final String key = String.format("event:%s", UUID.randomUUID().toString());
		logger.info("sendEvent: key={}, eventMessage={}", key, eventMessage);

		// Set hash attribute and push to queue in an atomic transaction
		Object obj = redisTemplate.execute(new SessionCallback() {
			@Override
			public Object execute(RedisOperations operations) throws DataAccessException {
				operations.multi();
				operations.opsForHash().put(key, "message", eventMessage);
				operations.opsForList().rightPush(EVENT_QUEUE, key);
				List<Object> response = operations.exec();
				return response;
			}
		});

		return Response.noContent().build();
	}

}
