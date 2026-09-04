package com.swamisuite.common.tenancy;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;

/**
 * Resolves the Hibernate tenant identifier from {@link TenantContext}.
 *
 * <p>This is Phase 0 scaffolding: it makes the multi-tenant Hibernate wiring
 * compile and be depended upon, but every request currently resolves to
 * {@link TenantContext#DEFAULT_SCHEMA}. Real per-tenant schema resolution (reading
 * the JWT {@code tenant_id} claim and mapping it to a provisioned schema) is
 * Phase 1+ work, per ADR 0001.
 */
public class DefaultCurrentTenantIdentifierResolver implements CurrentTenantIdentifierResolver<String> {

    @Override
    public String resolveCurrentTenantIdentifier() {
        return TenantContext.getTenantSchema();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }
}
