package com.swamisuite.identity.service;

import com.swamisuite.identity.domain.Permission;
import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.Role.RoleScope;
import com.swamisuite.identity.repository.PermissionRepository;
import com.swamisuite.identity.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Instantiates the TENANT_ADMIN/TENANT_MANAGER role templates (§4.1) for a newly
 * created tenant. Each tenant gets its own row (not a shared global one) so one
 * tenant's role edits never affect another's, per the note in
 * V7__seed_platform_roles_and_permissions.sql.
 */
@Service
public class RoleTemplateService {

    private static final List<String> TENANT_ADMIN_PERMISSIONS = List.of(
            "tenant:profile:read", "tenant:profile:update",
            "tenant:employee:invite", "tenant:employee:read", "tenant:employee:revoke",
            "tenant:role:create", "tenant:role:manage", "tenant:role:read",
            // Doc §15.7: only TENANT_ADMIN may start a checkout - seeded directly here,
            // not exposed through the custom role builder (RoleService filters every
            // platform:* permission out of GET /permissions), so it can't be delegated
            // to an arbitrary employee role.
            "platform:billing:purchase"
    );

    private static final List<String> TENANT_MANAGER_PERMISSIONS = List.of(
            "tenant:profile:read", "tenant:employee:read", "tenant:role:read"
    );

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleTemplateService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /** Creates TENANT_ADMIN and TENANT_MANAGER for a new tenant, returns TENANT_ADMIN. */
    public Role provisionDefaultRoles(UUID tenantId) {
        createRole(tenantId, "TENANT_MANAGER", TENANT_MANAGER_PERMISSIONS);
        return createRole(tenantId, "TENANT_ADMIN", TENANT_ADMIN_PERMISSIONS);
    }

    private Role createRole(UUID tenantId, String name, List<String> permissionCodes) {
        Role role = new Role(tenantId, name, RoleScope.TENANT, true);
        role.setPermissions(resolvePermissions(permissionCodes));
        return roleRepository.save(role);
    }

    private Set<Permission> resolvePermissions(List<String> codes) {
        Set<Permission> all = new HashSet<>(permissionRepository.findAll());
        Set<Permission> resolved = new HashSet<>();
        for (Permission permission : all) {
            if (codes.contains(permission.getCode())) {
                resolved.add(permission);
            }
        }
        return resolved;
    }
}
