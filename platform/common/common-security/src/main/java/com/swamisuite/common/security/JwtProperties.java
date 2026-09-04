package com.swamisuite.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Shared RS256 JWT settings. {@code accessTokenTtlSeconds} must stay at or below 900
 * (15 minutes) per PLATFORM_BUILD_INSTRUCTIONS.md §4.2.
 */
@ConfigurationProperties(prefix = "swamisuite.jwt")
public record JwtProperties(
        String issuer,
        long accessTokenTtlSeconds,
        long refreshTokenTtlSeconds
) {
}
