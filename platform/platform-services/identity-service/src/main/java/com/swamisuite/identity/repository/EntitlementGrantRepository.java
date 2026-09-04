package com.swamisuite.identity.repository;

import com.swamisuite.identity.domain.EntitlementGrant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntitlementGrantRepository extends JpaRepository<EntitlementGrant, UUID> {
    List<EntitlementGrant> findByTenantId(UUID tenantId);

    Optional<EntitlementGrant> findByTenantIdAndAppKey(UUID tenantId, String appKey);

    void deleteByTenantIdAndAppKey(UUID tenantId, String appKey);
}
