package com.swamisuite.common.audit;

import java.util.Map;
import java.util.UUID;

/** What gets posted to audit-service for every privileged action, per doc §12. */
public record AuditEvent(
        UUID actorId,
        ActorType actorType,
        UUID tenantId,
        String action,
        String resourceType,
        String resourceId,
        Map<String, Object> metadata
) {
    public enum ActorType {
        USER, SYSTEM
    }
}
