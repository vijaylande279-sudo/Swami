package com.swamisuite.tenant.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.swamisuite.tenant.domain.Tenant;
import com.swamisuite.tenant.repository.TenantRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Proves the Phase 1 cross-tenant isolation gate at the unit level: TenantService
 * never returns or acts on a tenant that doesn't match the caller's own tenant_id,
 * regardless of what tenant id a request path parameter asks for. This is the same
 * check every tenant-scoped controller method (@GetMapping/@PatchMapping on
 * /tenants/{id}, invites, employees) relies on via requireOwned().
 *
 * <p>A full end-to-end test (real HTTP calls between identity-service and
 * tenant-service via Testcontainers, per the Phase 1 plan) is a follow-up once
 * Docker is available to iterate on it - not run in this environment.
 */
@ExtendWith(MockitoExtension.class)
class TenantServiceCrossTenantTest {

    @Mock
    private TenantRepository tenantRepository;

    private TenantService tenantService;

    private UUID tenantAId;
    private UUID tenantBId;
    private Tenant tenantB;

    @BeforeEach
    void setUp() {
        tenantService = new TenantService(tenantRepository);
        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        tenantB = new Tenant("Tenant B", "tenant-b", "admin@tenant-b.example");
    }

    @Test
    void requireOwned_rejectsWhenCallerTenantDiffersFromRequestedTenant() {
        assertThatThrownBy(() -> tenantService.requireOwned(tenantAId, tenantBId))
                .isInstanceOf(TenantService.TenantException.class);
    }

    @Test
    void requireOwned_rejectsWhenCallerHasNoTenantContext() {
        assertThatThrownBy(() -> tenantService.requireOwned(null, tenantBId))
                .isInstanceOf(TenantService.TenantException.class);
    }

    @Test
    void requireOwned_succeedsOnlyForTheCallersOwnTenant() {
        when(tenantRepository.findById(tenantBId)).thenReturn(Optional.of(tenantB));

        Tenant result = tenantService.requireOwned(tenantBId, tenantBId);

        assertThat(result).isSameAs(tenantB);
    }

    @Test
    void getOwnTenant_neverLeaksAnotherTenantsProfile() {
        // Tenant A's caller asks for Tenant B's id - must fail identically to "not found",
        // never revealing that Tenant B exists.
        assertThatThrownBy(() -> tenantService.getOwnTenant(tenantAId, tenantBId))
                .isInstanceOf(TenantService.TenantException.class)
                .hasMessage("Tenant not found");
    }
}
