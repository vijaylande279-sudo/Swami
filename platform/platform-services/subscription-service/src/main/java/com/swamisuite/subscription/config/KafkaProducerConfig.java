package com.swamisuite.subscription.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swamisuite.common.events.DomainEvent;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, DomainEvent> domainEventProducerFactory(
            @Value("${spring.kafka.bootstrap-servers}") String bootstrapServers,
            @Qualifier("domainEventObjectMapper") ObjectMapper objectMapper) {
        Map<String, Object> config = Map.of(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class,
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class
        );
        DefaultKafkaProducerFactory<String, DomainEvent> factory = new DefaultKafkaProducerFactory<>(config);
        factory.setValueSerializer(new JsonSerializer<>(objectMapper));
        return factory;
    }

    @Bean
    public KafkaTemplate<String, DomainEvent> domainEventKafkaTemplate(ProducerFactory<String, DomainEvent> domainEventProducerFactory) {
        return new KafkaTemplate<>(domainEventProducerFactory);
    }
}
