package com.swamisuite.identity.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swamisuite.common.events.EntitlementChangedEvent;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, EntitlementChangedEvent> entitlementChangedConsumerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.kafka.consumer.group-id:identity-service}") String groupId,
            @Qualifier("domainEventObjectMapper") ObjectMapper objectMapper) {

        JsonDeserializer<EntitlementChangedEvent> valueDeserializer = new JsonDeserializer<>(EntitlementChangedEvent.class, objectMapper);
        valueDeserializer.addTrustedPackages("com.swamisuite.common.events");
        valueDeserializer.setUseTypeHeaders(false);

        Map<String, Object> config = Map.of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ConsumerConfig.GROUP_ID_CONFIG, groupId,
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"
        );

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(),
                new ErrorHandlingDeserializer<>(valueDeserializer));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, EntitlementChangedEvent> entitlementChangedListenerContainerFactory(
            ConsumerFactory<String, EntitlementChangedEvent> entitlementChangedConsumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, EntitlementChangedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(entitlementChangedConsumerFactory);
        return factory;
    }
}
