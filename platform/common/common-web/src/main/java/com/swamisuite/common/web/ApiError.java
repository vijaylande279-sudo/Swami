package com.swamisuite.common.web;

import java.time.Instant;

/**
 * Standard error body returned by every swami-suite service, so frontends can
 * handle failures uniformly regardless of which backend service produced them.
 */
public record ApiError(
        String code,
        String message,
        String traceId,
        Instant timestamp
) {
}
