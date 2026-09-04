package com.swamisuite.subscription.repository;

import com.swamisuite.subscription.domain.Subscription;
import com.swamisuite.subscription.domain.Subscription.SubscriptionStatus;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByTenantId(UUID tenantId);

    Optional<Subscription> findByTenantIdAndAppKey(UUID tenantId, String appKey);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByStatusAndCurrentPeriodEndBetween(SubscriptionStatus status, Instant from, Instant to);

    List<Subscription> findByStatusAndGraceUntilBefore(SubscriptionStatus status, Instant cutoff);
}
