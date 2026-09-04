package com.swamisuite.common.security;

import java.time.Instant;
import java.util.List;

/**
 * Shape of the claims every swami-suite JWT carries, per
 * PLATFORM_BUILD_INSTRUCTIONS.md §4.2. Populated by identity-service (Phase 1) and
 * read by {@code common-tenancy}'s tenant-resolving filter and per-service
 * {@code @PreAuthorize} checks in later phases.
 */
public record JwtClaims(
        String subject,
        String tenantId,
        List<String> roles,
        List<String> permissions,
        List<String> entitlements,
        Instant expiresAt
) {
}
