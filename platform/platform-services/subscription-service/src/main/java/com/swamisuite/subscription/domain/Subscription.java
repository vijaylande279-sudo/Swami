package com.swamisuite.subscription.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Per-app billing record - a tenant subscribing to two apps holds two Subscription
 * rows. This is the billing source of truth; tenant-service's Tenant.status is a
 * coarse rollup kept in sync via the internal status-sync endpoint, not the other
 * way around.
 */
@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
public class Subscription {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "app_key", nullable = false)
    private String appKey;

    @Column(name = "plan_id", nullable = false, columnDefinition = "uuid")
    private UUID planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.PENDING_PAYMENT;

    @Column(name = "current_period_start")
    private Instant currentPeriodStart;

    @Column(name = "current_period_end")
    private Instant currentPeriodEnd;

    /** 7-day grace after period end before suspension, per doc §6.1. */
    @Column(name = "grace_until")
    private Instant graceUntil;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Subscription(UUID tenantId, String appKey, UUID planId) {
        this.tenantId = tenantId;
        this.appKey = appKey;
        this.planId = planId;
    }

    public enum SubscriptionStatus {
        PENDING_PAYMENT, ACTIVE, PAST_DUE, SUSPENDED, CANCELLED
    }
}
