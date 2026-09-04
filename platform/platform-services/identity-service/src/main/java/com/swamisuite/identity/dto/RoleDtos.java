package com.swamisuite.identity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public final class RoleDtos {

    private RoleDtos() {
    }

    public record CreateRoleRequest(@NotBlank String name, List<String> permissionCodes) {
    }

    public record UpdateRolePermissionsRequest(@NotEmpty List<String> permissionCodes) {
    }

    public record RoleResponse(UUID id, UUID tenantId, String name, String scope,
                                boolean system, List<String> permissions) {
    }

    public record PermissionResponse(String code, String description) {
    }
}
