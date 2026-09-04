package com.swamisuite.payment.repository;

import com.swamisuite.payment.domain.Refund;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, UUID> {
}
