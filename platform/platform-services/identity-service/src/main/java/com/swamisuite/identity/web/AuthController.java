package com.swamisuite.identity.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.identity.domain.Permission;
import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.User;
import com.swamisuite.identity.dto.AuthDtos.*;
import com.swamisuite.identity.service.AuthService;
import com.swamisuite.identity.service.PasswordResetService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public TokenPairResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request.tenantName(), request.email(), request.password(), request.fullName());
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request.email(), request.password());
    }

    @PostMapping("/login/mfa")
    public TokenPairResponse loginMfa(@Valid @RequestBody MfaLoginRequest request) {
        return authService.completeMfaLogin(request.mfaChallengeId(), request.code());
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public void logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request.refreshToken());
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        JwtClaims claims = (JwtClaims) authentication.getPrincipal();
        User user = authService.requireUser(UUID.fromString(claims.subject()));
        return new MeResponse(
                user.getId(), user.getTenantId(), user.getEmail(), user.getFullName(),
                user.getRoles().stream().map(Role::getName).sorted().toList(),
                user.getRoles().stream().flatMap(r -> r.getPermissions().stream()).map(Permission::getCode).distinct().sorted().toList(),
                user.isMfaEnabled()
        );
    }

    @PostMapping("/password/forgot")
    public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.email());
    }

    @PostMapping("/password/reset")
    public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetPassword(request.token(), request.newPassword());
    }
}
