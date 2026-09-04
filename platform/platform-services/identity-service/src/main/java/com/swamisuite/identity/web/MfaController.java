package com.swamisuite.identity.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.identity.dto.AuthDtos.*;
import com.swamisuite.identity.service.AuthService;
import com.swamisuite.identity.service.MfaService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mfa")
public class MfaController {

    private final MfaService mfaService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public MfaController(MfaService mfaService, AuthService authService, PasswordEncoder passwordEncoder) {
        this.mfaService = mfaService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/enroll")
    public MfaEnrollResponse enroll(Authentication authentication) {
        return mfaService.enroll(currentUserId(authentication));
    }

    @PostMapping("/verify")
    public MfaVerifyResponse verify(Authentication authentication, @Valid @RequestBody MfaVerifyRequest request) {
        return new MfaVerifyResponse(mfaService.verify(currentUserId(authentication), request.code()));
    }

    @PostMapping("/disable")
    public void disable(Authentication authentication, @Valid @RequestBody MfaDisableRequest request) {
        UUID userId = currentUserId(authentication);
        var user = authService.requireUser(userId);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new AuthService.AuthException("Invalid password");
        }
        mfaService.disable(userId);
    }

    private UUID currentUserId(Authentication authentication) {
        return UUID.fromString(((JwtClaims) authentication.getPrincipal()).subject());
    }
}
