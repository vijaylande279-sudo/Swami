package com.swamisuite.identity.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "swamisuite.jwt.keys")
public record JwtKeyProperties(
        String privateKeyPath,
        String publicKeyPath
) {
}
