package com.swamisuite.subscription.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** The thing whose changes get published as entitlement.changed Kafka events. */
@Entity
@Table(name = "entitlements")
@Getter
@Setter
@NoArgsConstructor
public class Entitlement {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "app_key", nullable = false)
    private String appKey;

    @Column(name = "subscription_id", nullable = false, columnDefinition = "uuid")
    private UUID subscriptionId;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    @Column(name = "revoked_at")
    private Instant revokedAt;

    public Entitlement(UUID tenantId, String appKey, UUID subscriptionId) {
        this.tenantId = tenantId;
        this.appKey = appKey;
        this.subscriptionId = subscriptionId;
    }

    public boolean isActive() {
        return revokedAt == null;
    }
}
