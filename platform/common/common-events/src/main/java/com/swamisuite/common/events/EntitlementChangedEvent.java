package com.swamisuite.common.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.UUID;

/**
 * Published by subscription-service whenever a tenant's access to an app is granted
 * or revoked (activation, suspension, cancellation). identity-service consumes this
 * to maintain its own local entitlement read-model, per PLATFORM_BUILD_INSTRUCTIONS.md
 * §4.2/§6.2 - this is what "forces token refresh" to pick up the new entitlements.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntitlementChangedEvent extends DomainEvent {

    public static final String TYPE = "entitlement.changed";
    public static final String TOPIC = "swamisuite.entitlement.changed";

    private final String appKey;
    private final boolean granted;

    @JsonCreator
    public EntitlementChangedEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("tenantId") String tenantId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("appKey") String appKey,
            @JsonProperty("granted") boolean granted) {
        super(eventId, tenantId, occurredAt, TYPE);
        this.appKey = appKey;
        this.granted = granted;
    }

    public String getAppKey() {
        return appKey;
    }

    public boolean isGranted() {
        return granted;
    }
}
