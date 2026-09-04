package com.swamisuite.catalog.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Versioned pricing: a price change is a NEW row with a fresh effectiveFrom, never a
 * mutation of pricePaise on an existing row, per doc §7.3 - existing subscriptions
 * keep the price they were quoted until they renew onto whatever plan is effective
 * at that time. Phase 2 is annual-only: billingInterval only ever has one value.
 */
@Entity
@Table(name = "plans")
@Getter
@Setter
@NoArgsConstructor
public class Plan {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tier_id", nullable = false, columnDefinition = "uuid")
    private UUID tierId;

    @Column(name = "plan_key", nullable = false)
    private String planKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false)
    private BillingInterval billingInterval = BillingInterval.ANNUAL;

    /** Never a double - integer paise, per doc §7.3's hard rule. */
    @Column(name = "price_paise", nullable = false)
    private long pricePaise;

    @Column(name = "gst_rate_bps", nullable = false)
    private int gstRateBps;

    @Column(name = "effective_from", nullable = false)
    private Instant effectiveFrom = Instant.now();

    @Column(name = "effective_to")
    private Instant effectiveTo;

    public Plan(UUID tierId, String planKey, long pricePaise, int gstRateBps) {
        this.tierId = tierId;
        this.planKey = planKey;
        this.pricePaise = pricePaise;
        this.gstRateBps = gstRateBps;
    }

    public boolean isCurrentlyEffective() {
        Instant now = Instant.now();
        return !now.isBefore(effectiveFrom) && (effectiveTo == null || now.isBefore(effectiveTo));
    }

    public long gstPaise() {
        return Math.round(pricePaise * gstRateBps / 10_000.0);
    }

    public long totalPaise() {
        return pricePaise + gstPaise();
    }

    public enum BillingInterval {
        ANNUAL
    }
}
