package com.swamisuite.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(ServiceClientsConfig.ServiceUrlsProperties.class)
public class ServiceClientsConfig {

    @Bean
    public RestClient tenantServiceRestClient(ServiceUrlsProperties properties) {
        return RestClient.builder().baseUrl(properties.tenantServiceUrl()).build();
    }

    @ConfigurationProperties(prefix = "swamisuite.services")
    public record ServiceUrlsProperties(String tenantServiceUrl, String internalToken) {
    }
}
