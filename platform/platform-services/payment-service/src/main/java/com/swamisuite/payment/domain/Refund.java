package com.swamisuite.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/** Manual, super-admin/support-initiated only per doc §15.4/§15.10 - never automatic. */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
public class Refund {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "checkout_intent_id", nullable = false, columnDefinition = "uuid")
    private UUID checkoutIntentId;

    @Column(name = "razorpay_refund_id")
    private String razorpayRefundId;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RefundStatus status = RefundStatus.INITIATED;

    @Column(nullable = false)
    private String reason;

    @Column(name = "initiated_by", nullable = false, columnDefinition = "uuid")
    private UUID initiatedBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public Refund(UUID checkoutIntentId, long amountPaise, String reason, UUID initiatedBy) {
        this.checkoutIntentId = checkoutIntentId;
        this.amountPaise = amountPaise;
        this.reason = reason;
        this.initiatedBy = initiatedBy;
    }

    public enum RefundStatus {
        INITIATED, PROCESSED, FAILED
    }
}
