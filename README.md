I am not trying to use Spring to replace Dropwizard's way of handling beans and their dependencies. I agree with Dropwizard's philosophy that a big dependency-injection framework is unnecessary for most projects. I am using just enough Spring to satisfy the needs of Spring-Redis. And I never want to see a Spring XML file in this project. It's Java configuration all the way, baby!

## Redis Pub/Sub

I discovered that the Redis publish/subscribe feature does not support persistent messages. This means that if a message is published and no recipient is subscribed to the channel, the message will disappear without a trace.

## Running

	gradle build run

