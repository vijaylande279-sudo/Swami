package com.swamisuite.common.security;

import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires a {@link PublicKeySource} for any service that sets
 * {@code swamisuite.jwt.jwks-client.url} - opt-in, since identity-service (which
 * issues, not verifies, and holds its own key pair directly) doesn't need this.
 */
@Configuration
@EnableConfigurationProperties(JwksClientProperties.class)
@ConditionalOnProperty(prefix = "swamisuite.jwt.jwks-client", name = "url")
public class JwtVerificationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public PublicKeySource publicKeySource(JwksClientProperties properties) {
        long refreshSeconds = properties.refreshIntervalSeconds() > 0 ? properties.refreshIntervalSeconds() : 600;
        return new CachingJwksPublicKeySource(RestClient.create(), properties.url(), Duration.ofSeconds(refreshSeconds));
    }

    @Bean
    @ConditionalOnMissingBean
    public JwtVerifier jwtVerifier(PublicKeySource publicKeySource) {
        return new JwtVerifier(publicKeySource);
    }
}
