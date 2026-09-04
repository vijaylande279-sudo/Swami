package com.swamisuite.common.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Shared Jackson configuration for (de)serializing {@link DomainEvent} payloads on
 * Kafka topics. Individual services still declare their own producer/consumer
 * factories once real topics exist (from the services that need them, in later
 * phases); this only standardises the JSON mapping they share.
 */
@Configuration
public class KafkaJsonConfig {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper domainEventObjectMapper() {
        return new ObjectMapper().registerModule(new JavaTimeModule());
    }
}
