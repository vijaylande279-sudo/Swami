package com.swamisuite.identity.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** Local read-model of subscription-service's entitlement.changed events - what JwtIssuer reads for the JWT's entitlements claim. */
@Entity
@Table(name = "entitlement_grants")
@Getter
@Setter
@NoArgsConstructor
public class EntitlementGrant {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "app_key", nullable = false)
    private String appKey;

    @Column(name = "granted_at", nullable = false)
    private Instant grantedAt = Instant.now();

    public EntitlementGrant(UUID tenantId, String appKey) {
        this.tenantId = tenantId;
        this.appKey = appKey;
    }
}
