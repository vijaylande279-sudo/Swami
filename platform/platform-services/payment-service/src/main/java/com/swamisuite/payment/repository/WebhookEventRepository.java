package com.swamisuite.payment.repository;

import com.swamisuite.payment.domain.WebhookEvent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {
    Optional<WebhookEvent> findByRazorpayEventId(String razorpayEventId);
}
