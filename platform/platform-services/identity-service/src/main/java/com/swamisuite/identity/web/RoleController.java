package com.swamisuite.identity.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.identity.dto.RoleDtos.*;
import com.swamisuite.identity.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping("/roles")
    @PreAuthorize("hasAuthority('tenant:role:create')")
    public RoleResponse createRole(Authentication authentication, @Valid @RequestBody CreateRoleRequest request) {
        return roleService.createRole(currentTenantId(authentication), request);
    }

    @GetMapping("/roles")
    @PreAuthorize("hasAuthority('tenant:role:read')")
    public List<RoleResponse> listRoles(Authentication authentication) {
        return roleService.listRoles(currentTenantId(authentication));
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('tenant:role:manage')")
    public RoleResponse updatePermissions(Authentication authentication, @PathVariable UUID id,
                                           @Valid @RequestBody UpdateRolePermissionsRequest request) {
        return roleService.updatePermissions(currentTenantId(authentication), id, request.permissionCodes());
    }

    @GetMapping("/permissions")
    public List<PermissionResponse> listPermissions() {
        return roleService.listPermissions();
    }

    private UUID currentTenantId(Authentication authentication) {
        String tenantId = ((JwtClaims) authentication.getPrincipal()).tenantId();
        if (tenantId == null) {
            throw new com.swamisuite.identity.service.AuthService.AuthException("No tenant context");
        }
        return UUID.fromString(tenantId);
    }
}
