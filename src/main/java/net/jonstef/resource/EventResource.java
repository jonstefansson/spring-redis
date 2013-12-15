package net.jonstef.resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * @author Jon Stefansson
 */
@Path("/event")
public class EventResource {

	private final StringRedisTemplate redisTemplate;
	private final Logger logger = LoggerFactory.getLogger(EventResource.class);

	public EventResource(StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	@POST
	public Response sendEvent(@FormParam("message") String eventMessage) {
		logger.info("sendEvent: {}", eventMessage);
		String key = String.format("event:%d", redisTemplate.opsForValue().increment("key:event:sequence", 1l));
		logger.info("sendEvent: push key {}", key);
		redisTemplate.opsForList().rightPush("list:event:queue", key);
		return Response.noContent().build();
	}

}
