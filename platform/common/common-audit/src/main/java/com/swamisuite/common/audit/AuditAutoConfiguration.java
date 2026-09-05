package com.swamisuite.common.audit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/** Wires an AuditClient for any service that sets swamisuite.services.audit-service-url. */
@Configuration
@EnableConfigurationProperties(AuditProperties.class)
@ConditionalOnProperty(prefix = "swamisuite.services", name = "audit-service-url")
public class AuditAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AuditClient auditClient(AuditProperties properties) {
        RestClient restClient = RestClient.builder().baseUrl(properties.auditServiceUrl()).build();
        return new AuditClient(restClient, properties.internalToken());
    }
}
