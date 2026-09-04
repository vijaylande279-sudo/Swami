package com.swamisuite.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Jackson configuration for (de)serializing {@link DomainEvent} payloads on
 * Kafka topics. Individual services still declare their own producer/consumer
 * factories once real topics exist; this only standardises the JSON mapping they
 * share.
 *
 * <p>Deliberately named (not left as the unqualified default {@code ObjectMapper}
 * bean) so it never collides with Spring Boot's own auto-configured one, which every
 * web-starter service already has for its REST controllers - consumers that want
 * this specific mapper should inject it with
 * {@code @Qualifier("domainEventObjectMapper")}.
 */
@Configuration
public class KafkaJsonConfig {

    @Bean
    @ConditionalOnMissingBean(name = "domainEventObjectMapper")
    public ObjectMapper domainEventObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
