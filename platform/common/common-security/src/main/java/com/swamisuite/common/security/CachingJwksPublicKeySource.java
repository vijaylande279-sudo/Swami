package com.swamisuite.common.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.web.client.RestClient;

/**
 * Fetches identity-service's RS256 public key from its JWKS endpoint and caches it
 * in memory, refreshing on a fixed interval or when {@link #invalidate()} is called
 * (e.g. after a verification failure that looks like a signature mismatch).
 *
 * <p>Resolves identity-service by a plain configured base URL (its Docker Compose
 * service name/port, e.g. {@code http://identity-service:8081}) rather than through
 * Eureka's {@code lb://} scheme, to avoid pulling spring-cloud-loadbalancer into this
 * shared library - every service in Phase 1 runs on the same Docker network, so plain
 * DNS resolution is sufficient and keeps this class dependency-light.
 */
public class CachingJwksPublicKeySource implements PublicKeySource {

    private final RestClient restClient;
    private final String jwksUrl;
    private final Duration refreshInterval;
    private final AtomicReference<CachedKey> cache = new AtomicReference<>();

    public CachingJwksPublicKeySource(RestClient restClient, String jwksUrl, Duration refreshInterval) {
        this.restClient = restClient;
        this.jwksUrl = jwksUrl;
        this.refreshInterval = refreshInterval;
    }

    @Override
    public RSAPublicKey getCurrentKey() {
        CachedKey current = cache.get();
        if (current == null || current.isStale(refreshInterval)) {
            current = fetch();
            cache.set(current);
        }
        return current.key();
    }

    @Override
    public void invalidate() {
        cache.set(null);
    }

    private CachedKey fetch() {
        String body = restClient.get().uri(jwksUrl).retrieve().body(String.class);
        try {
            JWKSet jwkSet = JWKSet.parse(body);
            RSAKey rsaKey = (RSAKey) jwkSet.getKeys().get(0);
            return new CachedKey(rsaKey.toRSAPublicKey(), Instant.now());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to fetch/parse JWKS from " + jwksUrl, e);
        }
    }

    private record CachedKey(RSAPublicKey key, Instant fetchedAt) {
        boolean isStale(Duration refreshInterval) {
            return Instant.now().isAfter(fetchedAt.plus(refreshInterval));
        }
    }
}
