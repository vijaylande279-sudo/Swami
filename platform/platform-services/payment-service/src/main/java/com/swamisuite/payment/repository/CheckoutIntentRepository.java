package com.swamisuite.payment.repository;

import com.swamisuite.payment.domain.CheckoutIntent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutIntentRepository extends JpaRepository<CheckoutIntent, UUID> {
    Optional<CheckoutIntent> findByRazorpayOrderId(String razorpayOrderId);
}
