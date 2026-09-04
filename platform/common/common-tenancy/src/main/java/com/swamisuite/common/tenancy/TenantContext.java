package com.swamisuite.common.tenancy;

/**
 * Holds the current request's tenant schema identifier, populated by
 * {@link TenantContextFilter} from the JWT {@code tenant_id} claim (or a header, for
 * services not yet behind full JWT auth) and read by Hibernate's tenant resolver.
 *
 * <p>Phase 0 only provides the holder itself; nothing yet reads a real tenant claim
 * or resolves a real per-tenant schema — see {@link DefaultCurrentTenantIdentifierResolver}.
 */
public final class TenantContext {

    public static final String DEFAULT_SCHEMA = "public";

    private static final ThreadLocal<String> CURRENT_TENANT = ThreadLocal.withInitial(() -> DEFAULT_SCHEMA);

    private TenantContext() {
    }

    public static String getTenantSchema() {
        return CURRENT_TENANT.get();
    }

    public static void setTenantSchema(String schema) {
        CURRENT_TENANT.set(schema == null || schema.isBlank() ? DEFAULT_SCHEMA : schema);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
