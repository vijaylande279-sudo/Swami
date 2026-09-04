package com.swamisuite.subscription.repository;

import com.swamisuite.subscription.domain.Entitlement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementRepository extends JpaRepository<Entitlement, UUID> {
    Optional<Entitlement> findByTenantIdAndAppKeyAndRevokedAtIsNull(UUID tenantId, String appKey);

    List<Entitlement> findByTenantIdAndRevokedAtIsNull(UUID tenantId);

    List<Entitlement> findBySubscriptionId(UUID subscriptionId);
}
