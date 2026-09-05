package com.swamisuite.audit.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class AuditDtos {

    private AuditDtos() {
    }

    public record RecordEventRequest(
            UUID actorId,
            @NotNull String actorType,
            UUID tenantId,
            @NotBlank String action,
            @NotBlank String resourceType,
            String resourceId,
            Map<String, Object> metadata
    ) {
    }

    public record AuditEventResponse(
            UUID id, UUID actorId, String actorType, UUID tenantId,
            String action, String resourceType, String resourceId,
            Map<String, Object> metadata, Instant createdAt
    ) {
    }
}
