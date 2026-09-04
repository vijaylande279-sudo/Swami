package com.swamisuite.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/** Request/response records for the auth API surface. */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank String tenantName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            String fullName
    ) {
    }

    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {
    }

    public record LoginResponse(
            boolean mfaRequired,
            String mfaChallengeId,
            String accessToken,
            String refreshToken
    ) {
    }

    public record MfaLoginRequest(@NotBlank String mfaChallengeId, @NotBlank String code) {
    }

    public record RefreshRequest(@NotBlank String refreshToken) {
    }

    public record TokenPairResponse(String accessToken, String refreshToken) {
    }

    public record MeResponse(UUID id, UUID tenantId, String email, String fullName,
                              List<String> roles, List<String> permissions, boolean mfaEnabled) {
    }

    public record ForgotPasswordRequest(@NotBlank @Email String email) {
    }

    public record ResetPasswordRequest(@NotBlank String token, @NotBlank @Size(min = 8) String newPassword) {
    }

    public record MfaEnrollResponse(String secret, String otpAuthUri) {
    }

    public record MfaVerifyRequest(@NotBlank String code) {
    }

    public record MfaVerifyResponse(List<String> backupCodes) {
    }

    public record MfaDisableRequest(@NotBlank String password) {
    }
}
