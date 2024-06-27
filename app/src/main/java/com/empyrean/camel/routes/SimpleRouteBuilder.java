package com.empyrean.camel.routes;

import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

import java.time.Instant;
import java.util.UUID;

@Slf4j
public class SimpleRouteBuilder extends RouteBuilder {
    public void configure() {
        log.info("Configuring Camel Route {}", this.getClass().getSimpleName());
        from("timer://job?period=PT1S")
                .process(p -> p.getMessage().setBody(UUID.randomUUID().toString()))
                .process(exchange -> {
                    System.out.println("Timer period: " + Instant.now() + " Message: " + exchange.getMessage().getBody());
                });

        log.info("Successfully Configured Camel Route {}", this.getClass().getSimpleName());
    }
}
