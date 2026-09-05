package com.swamisuite.audit.repository;

import com.swamisuite.audit.domain.AuditEvent;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

/**
 * Deliberately NOT a JpaRepository - that interface exposes delete()/deleteAll(),
 * and an audit log must have no delete path anywhere in code, per doc §12/§15.7.
 * Only the methods actually declared here exist.
 */
public interface AuditEventRepository extends Repository<AuditEvent, UUID> {

    AuditEvent save(AuditEvent event);

    Optional<AuditEvent> findById(UUID id);

    Page<AuditEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditEvent> findByTenantIdOrderByCreatedAtDesc(UUID tenantId, Pageable pageable);

    Page<AuditEvent> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);

    Page<AuditEvent> findByTenantIdAndActionOrderByCreatedAtDesc(UUID tenantId, String action, Pageable pageable);
}
