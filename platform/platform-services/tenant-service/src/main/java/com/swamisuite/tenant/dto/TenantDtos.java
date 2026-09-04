package com.swamisuite.tenant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public final class TenantDtos {

    private TenantDtos() {
    }

    public record TenantResponse(UUID id, String name, String slug, String gstin,
                                  String primaryContactEmail, String primaryContactPhone,
                                  String status, Instant trialEndsAt) {
    }

    public record UpdateTenantRequest(String name, String gstin, String primaryContactPhone) {
    }

    public record UpdateStatusRequest(@NotBlank String status) {
    }

    public record InviteEmployeeRequest(@NotBlank @Email String email, @NotBlank String roleName) {
    }

    /**
     * inviteToken is only populated on creation (the one moment the raw token exists) -
     * dev-mode stand-in for emailing an accept link, since notification-service doesn't
     * exist yet. Never populated when listing existing invites.
     */
    public record InviteResponse(UUID id, String email, String roleName, String status,
                                  Instant expiresAt, String inviteToken) {
    }

    public record AcceptInviteRequest(@NotBlank String password, String fullName) {
    }

    public record EmployeeResponse(UUID userId, String email, String fullName) {
    }
}
