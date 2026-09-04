package com.swamisuite.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

/** Service-to-service payloads for /internal/** endpoints - never routed through the gateway. */
public final class InternalDtos {

    private InternalDtos() {
    }

    public record CreateUserRequest(
            UUID tenantId,
            @NotBlank @Email String email,
            @NotBlank String password,
            String fullName,
            @NotBlank String roleName
    ) {
    }

    public record UserSummary(UUID id, UUID tenantId, String email, String fullName) {
    }

    public record CreateTenantRequest(@NotBlank String name, @NotBlank @Email String primaryContactEmail) {
    }

    public record TenantSummary(UUID id, String name, String slug, String status) {
    }
}
