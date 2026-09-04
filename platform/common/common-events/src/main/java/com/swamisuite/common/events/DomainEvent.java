package com.swamisuite.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Base envelope for every domain event published to Kafka across swami-suite
 * services, per PLATFORM_BUILD_INSTRUCTIONS.md §6.2 / §7.1 (e.g. {@code
 * entitlement.changed}, {@code payment.succeeded}). Concrete event types extend
 * this with their own payload.
 */
public abstract class DomainEvent {

    private final UUID eventId;
    private final String tenantId;
    private final Instant occurredAt;
    private final String type;

    protected DomainEvent(UUID eventId, String tenantId, Instant occurredAt, String type) {
        this.eventId = eventId;
        this.tenantId = tenantId;
        this.occurredAt = occurredAt;
        this.type = type;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public String getType() {
        return type;
    }
}
