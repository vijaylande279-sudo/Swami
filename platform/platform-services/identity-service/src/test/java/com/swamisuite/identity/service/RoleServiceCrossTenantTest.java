package com.swamisuite.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.Role.RoleScope;
import com.swamisuite.identity.repository.PermissionRepository;
import com.swamisuite.identity.repository.RoleRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Proves a Tenant Admin can never read or edit a custom role that belongs to a
 * different tenant, even when they know (or guess) its id - the same check the
 * custom-role-builder API (/roles/{id}/permissions) relies on. Part of the Phase 1
 * cross-tenant isolation gate, alongside tenant-service's equivalent test.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceCrossTenantTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private RoleService roleService;

    private UUID tenantAId;
    private UUID tenantBId;
    private Role tenantBRole;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository, permissionRepository);
        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        tenantBRole = new Role(tenantBId, "Custom Support Role", RoleScope.TENANT, false);
    }

    @Test
    void requireOwnedRole_rejectsARoleBelongingToAnotherTenant() {
        when(roleRepository.findById(tenantBRole.getId())).thenReturn(Optional.of(tenantBRole));

        assertThatThrownBy(() -> roleService.requireOwnedRole(tenantAId, tenantBRole.getId()))
                .isInstanceOf(AuthService.AuthException.class)
                .hasMessage("Role not found");
    }

    @Test
    void requireOwnedRole_succeedsForTheOwningTenant() {
        when(roleRepository.findById(tenantBRole.getId())).thenReturn(Optional.of(tenantBRole));

        Role result = roleService.requireOwnedRole(tenantBId, tenantBRole.getId());

        assertThat(result).isSameAs(tenantBRole);
    }

    @Test
    void updatePermissions_rejectsEditingAnotherTenantsRole() {
        when(roleRepository.findById(tenantBRole.getId())).thenReturn(Optional.of(tenantBRole));

        assertThatThrownBy(() -> roleService.updatePermissions(tenantAId, tenantBRole.getId(), java.util.List.of("tenant:role:read")))
                .isInstanceOf(AuthService.AuthException.class);
    }
}
