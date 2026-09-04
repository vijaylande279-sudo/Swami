package com.swamisuite.tenant.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "tenants")
@Getter
@Setter
@NoArgsConstructor
public class Tenant {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    private String gstin;

    @Column(name = "primary_contact_email", nullable = false)
    private String primaryContactEmail;

    @Column(name = "primary_contact_phone")
    private String primaryContactPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status = TenantStatus.TRIALING;

    @Column(name = "trial_ends_at")
    private Instant trialEndsAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Tenant(String name, String slug, String primaryContactEmail) {
        this.name = name;
        this.slug = slug;
        this.primaryContactEmail = primaryContactEmail;
        this.trialEndsAt = Instant.now().plusSeconds(14L * 24 * 3600);
    }

    /** §6.1 lifecycle. Phase 1 only reaches TRIALING automatically; other states are manual/testing overrides until Phase 2's scheduled jobs exist. */
    public enum TenantStatus {
        TRIALING, PENDING_PAYMENT, ACTIVE, PAST_DUE, SUSPENDED, CANCELLED
    }
}
