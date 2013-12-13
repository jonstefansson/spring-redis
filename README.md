I am not trying to use Spring to replace Dropwizard's way of handling beans and their dependencies. I agree with Dropwizard's philosophy that a big dependency-injection framework is unnecessary for most projects. I am using just enough Spring to satisfy the needs of Spring-Redis. And I never want to see a Spring XML file in this project. It's Java configuration all the way, baby!

## Running

	gradle compile run

