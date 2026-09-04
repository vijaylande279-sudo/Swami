package com.swamisuite.tenant.repository;

import com.swamisuite.tenant.domain.TenantInvite;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantInviteRepository extends JpaRepository<TenantInvite, UUID> {
    Optional<TenantInvite> findByTokenHash(String tokenHash);

    List<TenantInvite> findByTenantId(UUID tenantId);
}
