package com.swamisuite.subscription.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * subscription-service's own fast-path cache for its own APIs - Redis's first real
 * use in the platform. NOT what identity-service reads for JWT issuance (that's its
 * own local read-model populated via Kafka); this is purely subscription-service's
 * cache of what it already knows. TTL capped at 900s (matches access-token TTL) as a
 * safety net even without perfect invalidation.
 */
@Service
public class EntitlementCacheService {

    private static final Logger log = LoggerFactory.getLogger(EntitlementCacheService.class);
    private static final Duration TTL = Duration.ofSeconds(900);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public EntitlementCacheService(StringRedisTemplate redisTemplate,
                                    @Qualifier("domainEventObjectMapper") ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void writeThrough(UUID tenantId, List<String> activeAppKeys) {
        try {
            String json = objectMapper.writeValueAsString(activeAppKeys);
            redisTemplate.opsForValue().set(key(tenantId), json, TTL);
        } catch (Exception e) {
            log.warn("Failed to write entitlement cache for tenant {}: {}", tenantId, e.getMessage());
        }
    }

    private String key(UUID tenantId) {
        return "entitlement:" + tenantId;
    }
}
