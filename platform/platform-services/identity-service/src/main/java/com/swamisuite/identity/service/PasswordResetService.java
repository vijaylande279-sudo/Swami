package com.swamisuite.identity.service;

import com.swamisuite.identity.domain.PasswordResetToken;
import com.swamisuite.identity.domain.User;
import com.swamisuite.identity.repository.PasswordResetTokenRepository;
import com.swamisuite.identity.repository.UserRepository;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetService {

    private static final long TOKEN_TTL_SECONDS = 30 * 60;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ResetLinkDeliverer resetLinkDeliverer;
    private final SecureRandom random = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                 PasswordEncoder passwordEncoder, ResetLinkDeliverer resetLinkDeliverer) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.resetLinkDeliverer = resetLinkDeliverer;
    }

    /** Always succeeds from the caller's point of view, even for an unknown email - avoids account enumeration. */
    public void requestReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            byte[] bytes = new byte[32];
            random.nextBytes(bytes);
            String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            tokenRepository.save(new PasswordResetToken(user.getId(), hash(rawToken),
                    Instant.now().plusSeconds(TOKEN_TTL_SECONDS)));
            resetLinkDeliverer.deliver(user.getEmail(), "/auth/password/reset?token=" + rawToken);
        });
    }

    public void resetPassword(String rawToken, String newPassword) {
        PasswordResetToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .filter(PasswordResetToken::isUsable)
                .orElseThrow(() -> new AuthService.AuthException("Invalid or expired reset token"));

        User user = userRepository.findById(token.getUserId()).orElseThrow();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        token.setUsedAt(Instant.now());
        tokenRepository.save(token);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(raw.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
