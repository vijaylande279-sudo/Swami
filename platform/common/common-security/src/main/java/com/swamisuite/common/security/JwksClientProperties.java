package com.swamisuite.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Where a JWT-verifying service finds identity-service's JWKS endpoint, e.g.
 * {@code http://identity-service:8081/.well-known/jwks.json} in Docker Compose.
 */
@ConfigurationProperties(prefix = "swamisuite.jwt.jwks-client")
public record JwksClientProperties(
        String url,
        long refreshIntervalSeconds
) {
}
