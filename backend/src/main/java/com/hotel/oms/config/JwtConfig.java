package com.hotel.oms.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.charset.StandardCharsets;

@Slf4j
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtConfig {
    private String secret;
    private long expirationMs;

    @PostConstruct
    void validateSecret() {
        int byteLength = secret == null ? 0 : secret.getBytes(StandardCharsets.UTF_8).length;
        log.info("JWT secret length: {} bytes (minimum 32 required for HS256)", byteLength);
        if (byteLength < 32) {
            throw new IllegalStateException(
                    "app.jwt.secret (JWT_SECRET) must be at least 32 bytes long, got " + byteLength
                            + ". Set a longer JWT_SECRET environment variable.");
        }
    }
}
