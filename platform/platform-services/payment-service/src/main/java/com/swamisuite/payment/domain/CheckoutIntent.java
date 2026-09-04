package com.swamisuite.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * Server-computed checkout intent, per doc §15.8/§15.1: the amount here is always
 * what subscription-service (which itself re-fetches from catalog-service) returned
 * - never a client-supplied value. One intent -> one Razorpay order.
 */
@Entity
@Table(name = "checkout_intents")
@Getter
@Setter
@NoArgsConstructor
public class CheckoutIntent {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(name = "subscription_id", nullable = false, columnDefinition = "uuid")
    private UUID subscriptionId;

    @Column(name = "app_key", nullable = false)
    private String appKey;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @Column(name = "gst_paise", nullable = false)
    private long gstPaise;

    @Column(name = "total_paise", nullable = false)
    private long totalPaise;

    @Column(name = "razorpay_order_id")
    private String razorpayOrderId;

    /** Populated at webhook time - refunds are issued against a payment id, not the order id. */
    @Column(name = "razorpay_payment_id")
    private String razorpayPaymentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckoutIntentStatus status = CheckoutIntentStatus.CREATED;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public CheckoutIntent(UUID tenantId, UUID subscriptionId, String appKey, long amountPaise, long gstPaise, long totalPaise) {
        this.tenantId = tenantId;
        this.subscriptionId = subscriptionId;
        this.appKey = appKey;
        this.amountPaise = amountPaise;
        this.gstPaise = gstPaise;
        this.totalPaise = totalPaise;
    }

    public enum CheckoutIntentStatus {
        CREATED, PAID, FAILED
    }
}
