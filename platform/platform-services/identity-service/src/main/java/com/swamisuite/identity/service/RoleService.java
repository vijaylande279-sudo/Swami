package com.swamisuite.identity.service;

import com.swamisuite.identity.domain.Permission;
import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.Role.RoleScope;
import com.swamisuite.identity.dto.RoleDtos.CreateRoleRequest;
import com.swamisuite.identity.dto.RoleDtos.PermissionResponse;
import com.swamisuite.identity.dto.RoleDtos.RoleResponse;
import com.swamisuite.identity.repository.PermissionRepository;
import com.swamisuite.identity.repository.RoleRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** The "custom role builder" - a Tenant Admin composes a role from the platform's permission catalog. */
@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public RoleService(RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    public RoleResponse createRole(UUID tenantId, CreateRoleRequest request) {
        Role role = new Role(tenantId, request.name(), RoleScope.TENANT, false);
        role.setPermissions(resolvePermissions(request.permissionCodes()));
        return toResponse(roleRepository.save(role));
    }

    public List<RoleResponse> listRoles(UUID tenantId) {
        return roleRepository.findByTenantId(tenantId).stream().map(this::toResponse).toList();
    }

    public RoleResponse updatePermissions(UUID tenantId, UUID roleId, List<String> permissionCodes) {
        Role role = requireOwnedRole(tenantId, roleId);
        if (role.isSystem()) {
            throw new AuthService.AuthException("System roles cannot be edited");
        }
        role.setPermissions(resolvePermissions(permissionCodes));
        return toResponse(roleRepository.save(role));
    }

    /** Only permissions a Tenant Admin may actually assign - never platform:* (cross-tenant, PLATFORM_SUPER_ADMIN/PLATFORM_SUPPORT only). */
    public List<PermissionResponse> listPermissions() {
        return permissionRepository.findAll().stream()
                .filter(p -> isTenantAssignable(p.getCode()))
                .map(p -> new PermissionResponse(p.getCode(), p.getDescription()))
                .toList();
    }

    public Role requireOwnedRole(UUID tenantId, UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new AuthService.AuthException("Role not found"));
        if (!tenantId.equals(role.getTenantId())) {
            // Cross-tenant access attempt - never reveal whether the role exists elsewhere.
            throw new AuthService.AuthException("Role not found");
        }
        return role;
    }

    /**
     * Server-side enforcement (not just hiding platform:* from the UI list): a
     * Tenant Admin must never be able to grant a platform-scoped permission to a
     * custom role, even by crafting the request directly.
     */
    private Set<Permission> resolvePermissions(List<String> codes) {
        if (codes == null) {
            return new HashSet<>();
        }
        for (String code : codes) {
            if (!isTenantAssignable(code)) {
                throw new AuthService.AuthException("Permission not assignable to a tenant role: " + code);
            }
        }
        Set<Permission> resolved = new HashSet<>();
        for (Permission permission : permissionRepository.findAll()) {
            if (codes.contains(permission.getCode())) {
                resolved.add(permission);
            }
        }
        return resolved;
    }

    private boolean isTenantAssignable(String permissionCode) {
        return !permissionCode.startsWith("platform:");
    }

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(
                role.getId(), role.getTenantId(), role.getName(), role.getScope().name(), role.isSystem(),
                role.getPermissions().stream().map(Permission::getCode).sorted().toList()
        );
    }
}
