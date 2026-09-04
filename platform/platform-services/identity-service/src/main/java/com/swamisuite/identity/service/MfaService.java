package com.swamisuite.identity.service;

import com.swamisuite.identity.domain.MfaBackupCode;
import com.swamisuite.identity.domain.User;
import com.swamisuite.identity.dto.AuthDtos.MfaEnrollResponse;
import com.swamisuite.identity.repository.MfaBackupCodeRepository;
import com.swamisuite.identity.repository.UserRepository;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private static final int BACKUP_CODE_COUNT = 10;

    private final UserRepository userRepository;
    private final MfaBackupCodeRepository backupCodeRepository;
    private final TotpService totpService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public MfaService(UserRepository userRepository, MfaBackupCodeRepository backupCodeRepository,
                       TotpService totpService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.backupCodeRepository = backupCodeRepository;
        this.totpService = totpService;
        this.passwordEncoder = passwordEncoder;
    }

    /** Generates a secret and stores it un-activated until {@link #verify} confirms the first code. */
    public MfaEnrollResponse enroll(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        String secret = totpService.generateSecret();
        user.setMfaSecretEncrypted(secret);
        user.setMfaEnabled(false);
        userRepository.save(user);
        return new MfaEnrollResponse(secret, totpService.provisioningUri(secret, user.getEmail()));
    }

    public List<String> verify(UUID userId, String code) {
        User user = userRepository.findById(userId).orElseThrow();
        if (user.getMfaSecretEncrypted() == null || !totpService.verifyCode(user.getMfaSecretEncrypted(), code)) {
            throw new AuthService.AuthException("Invalid MFA code");
        }
        user.setMfaEnabled(true);
        userRepository.save(user);

        backupCodeRepository.deleteByUserId(userId);
        List<String> rawCodes = new ArrayList<>();
        for (int i = 0; i < BACKUP_CODE_COUNT; i++) {
            String rawCode = generateBackupCode();
            rawCodes.add(rawCode);
            backupCodeRepository.save(new MfaBackupCode(userId, passwordEncoder.encode(rawCode)));
        }
        return rawCodes;
    }

    /** Password re-confirmation is checked by the caller before invoking this. */
    public void disable(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setMfaEnabled(false);
        user.setMfaSecretEncrypted(null);
        userRepository.save(user);
        backupCodeRepository.deleteByUserId(userId);
    }

    private String generateBackupCode() {
        int value = 10_000_000 + random.nextInt(90_000_000);
        return String.valueOf(value);
    }
}
