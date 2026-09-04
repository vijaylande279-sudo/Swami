package com.swamisuite.payment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

/**
 * The idempotency record for Razorpay webhook deliveries, per doc §7.2/§15.7: a
 * unique constraint on razorpayEventId means a duplicate delivery hits a constraint
 * violation, caught and answered 200 without reprocessing - Razorpay retries, so
 * every webhook must be assumed to arrive at least twice.
 */
@Entity
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEvent {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "razorpay_event_id", nullable = false, unique = true)
    private String razorpayEventId;

    @Column(nullable = false)
    private String payload;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    public WebhookEvent(String razorpayEventId, String payload) {
        this.razorpayEventId = razorpayEventId;
        this.payload = payload;
    }
}
