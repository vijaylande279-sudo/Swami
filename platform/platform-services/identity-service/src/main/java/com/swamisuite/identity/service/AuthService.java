package com.swamisuite.identity.service;

import com.swamisuite.identity.domain.MfaBackupCode;
import com.swamisuite.identity.domain.RefreshToken;
import com.swamisuite.identity.domain.Role;
import com.swamisuite.identity.domain.User;
import com.swamisuite.identity.dto.AuthDtos.LoginResponse;
import com.swamisuite.identity.dto.AuthDtos.TokenPairResponse;
import com.swamisuite.identity.repository.MfaBackupCodeRepository;
import com.swamisuite.identity.repository.RefreshTokenRepository;
import com.swamisuite.identity.repository.RoleRepository;
import com.swamisuite.identity.repository.UserRepository;
import com.swamisuite.identity.security.JwtIssuer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtIssuer jwtIssuer;
    private final RoleTemplateService roleTemplateService;
    private final TenantServiceClient tenantServiceClient;
    private final TotpService totpService;
    private final MfaBackupCodeRepository backupCodeRepository;
    private final SecureRandom random = new SecureRandom();

    /** In-memory MFA login challenges: challengeId -> userId. 5-minute TTL enforced on read. */
    private final Map<String, PendingMfaChallenge> mfaChallenges = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, RoleRepository roleRepository,
                        RefreshTokenRepository refreshTokenRepository, PasswordEncoder passwordEncoder,
                        JwtIssuer jwtIssuer, RoleTemplateService roleTemplateService,
                        TenantServiceClient tenantServiceClient, TotpService totpService,
                        MfaBackupCodeRepository backupCodeRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtIssuer = jwtIssuer;
        this.roleTemplateService = roleTemplateService;
        this.tenantServiceClient = tenantServiceClient;
        this.totpService = totpService;
        this.backupCodeRepository = backupCodeRepository;
    }

    public TokenPairResponse register(String tenantName, String email, String rawPassword, String fullName) {
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthException("Email already registered");
        }
        var tenant = tenantServiceClient.createTenant(tenantName, email);
        Role tenantAdminRole = roleTemplateService.provisionDefaultRoles(tenant.id());

        User user = new User(tenant.id(), email.toLowerCase(), passwordEncoder.encode(rawPassword), fullName);
        user.setRoles(Set.of(tenantAdminRole));
        user = userRepository.save(user);

        return issueTokenPair(user);
    }

    public LoginResponse login(String email, String rawPassword) {
        User user = findByEmail(email).orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        if (user.isMfaEnabled()) {
            String challengeId = randomToken();
            mfaChallenges.put(challengeId, new PendingMfaChallenge(user.getId(), Instant.now().plusSeconds(300)));
            return new LoginResponse(true, challengeId, null, null);
        }

        TokenPairResponse tokens = issueTokenPair(user);
        return new LoginResponse(false, null, tokens.accessToken(), tokens.refreshToken());
    }

    public TokenPairResponse completeMfaLogin(String challengeId, String code) {
        PendingMfaChallenge challenge = mfaChallenges.get(challengeId);
        if (challenge == null || Instant.now().isAfter(challenge.expiresAt())) {
            mfaChallenges.remove(challengeId);
            throw new AuthException("MFA challenge expired or invalid");
        }
        User user = userRepository.findById(challenge.userId()).orElseThrow(() -> new AuthException("Invalid credentials"));
        if (!totpService.verifyCode(user.getMfaSecretEncrypted(), code) && !consumeBackupCodeIfValid(user.getId(), code)) {
            throw new AuthException("Invalid MFA code");
        }
        mfaChallenges.remove(challengeId);
        return issueTokenPair(user);
    }

    private boolean consumeBackupCodeIfValid(UUID userId, String code) {
        for (MfaBackupCode backupCode : backupCodeRepository.findByUserIdAndUsedAtIsNull(userId)) {
            if (passwordEncoder.matches(code, backupCode.getCodeHash())) {
                backupCode.setUsedAt(Instant.now());
                backupCodeRepository.save(backupCode);
                return true;
            }
        }
        return false;
    }

    public TokenPairResponse refresh(String rawRefreshToken) {
        String hash = hashToken(rawRefreshToken);
        RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new AuthException("Invalid refresh token"));

        if (existing.getRevokedAt() != null) {
            // Reuse of an already-rotated token: revoke the whole family (theft detection).
            refreshTokenRepository.findByFamilyId(existing.getFamilyId())
                    .forEach(t -> t.setRevokedAt(Instant.now()));
            throw new AuthException("Refresh token reuse detected - session revoked");
        }
        if (!existing.isActive()) {
            throw new AuthException("Refresh token expired");
        }

        User user = userRepository.findById(existing.getUserId()).orElseThrow(() -> new AuthException("Invalid refresh token"));
        String accessToken = jwtIssuer.issueAccessToken(user);
        String newRawRefreshToken = randomToken();
        RefreshToken next = new RefreshToken(user.getId(), hashToken(newRawRefreshToken), existing.getFamilyId(),
                Instant.now().plusSeconds(jwtIssuer.refreshTokenTtlSeconds()));
        next = refreshTokenRepository.save(next);

        existing.setRevokedAt(Instant.now());
        existing.setReplacedBy(next.getId());
        refreshTokenRepository.save(existing);

        return new TokenPairResponse(accessToken, newRawRefreshToken);
    }

    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByTokenHash(hashToken(rawRefreshToken))
                .ifPresent(t -> {
                    t.setRevokedAt(Instant.now());
                    refreshTokenRepository.save(t);
                });
    }

    public User requireUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new AuthException("User not found"));
    }

    Optional<User> findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }

    private TokenPairResponse issueTokenPair(User user) {
        String accessToken = jwtIssuer.issueAccessToken(user);
        String rawRefreshToken = randomToken();
        UUID familyId = UUID.randomUUID();
        RefreshToken refreshToken = new RefreshToken(user.getId(), hashToken(rawRefreshToken), familyId,
                Instant.now().plusSeconds(jwtIssuer.refreshTokenTtlSeconds()));
        refreshTokenRepository.save(refreshToken);
        return new TokenPairResponse(accessToken, rawRefreshToken);
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Deterministic (not BCrypt) so a stored token can be looked up by its hash -
     * safe here since these are 32 random bytes, not user-chosen secrets.
     */
    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(rawToken.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private record PendingMfaChallenge(UUID userId, Instant expiresAt) {
    }

    public static class AuthException extends RuntimeException {
        public AuthException(String message) {
            super(message);
        }
    }
}
