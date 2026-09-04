package com.swamisuite.identity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.swamisuite.identity.domain.Permission;
import com.swamisuite.identity.dto.RoleDtos.CreateRoleRequest;
import com.swamisuite.identity.repository.PermissionRepository;
import com.swamisuite.identity.repository.RoleRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Found via manual UI testing: a Tenant Admin was able to create a custom role
 * carrying `platform:tenant:read`, a platform-scoped permission meant only for
 * PLATFORM_SUPER_ADMIN/PLATFORM_SUPPORT. Proves that gap is closed, both for what
 * the role builder offers (listPermissions) and what the API actually accepts
 * (createRole/updatePermissions) - the latter matters even if the UI never offers
 * the choice, since a crafted request must still be rejected.
 */
@ExtendWith(MockitoExtension.class)
class RoleServicePermissionScopeTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PermissionRepository permissionRepository;

    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleService(roleRepository, permissionRepository);
    }

    @Test
    void listPermissions_excludesPlatformScopedPermissions() {
        when(permissionRepository.findAll()).thenReturn(List.of(
                new Permission("platform:tenant:read", "platform"),
                new Permission("tenant:employee:read", "tenant")
        ));

        var result = roleService.listPermissions();

        assertThat(result).extracting("code").containsExactly("tenant:employee:read");
    }

    @Test
    void createRole_rejectsAPlatformScopedPermission() {
        UUID tenantId = UUID.randomUUID();

        assertThatThrownBy(() -> roleService.createRole(tenantId, new CreateRoleRequest("Waiter", List.of("platform:tenant:read"))))
                .isInstanceOf(AuthService.AuthException.class);
    }
}
