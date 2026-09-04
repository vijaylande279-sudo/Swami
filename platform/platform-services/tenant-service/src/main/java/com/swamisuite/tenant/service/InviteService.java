package com.swamisuite.tenant.service;

import com.swamisuite.tenant.domain.TenantInvite;
import com.swamisuite.tenant.domain.TenantInvite.InviteStatus;
import com.swamisuite.tenant.dto.InternalDtos.CreateUserRequest;
import com.swamisuite.tenant.dto.InternalDtos.UserSummary;
import com.swamisuite.tenant.dto.TenantDtos.EmployeeResponse;
import com.swamisuite.tenant.dto.TenantDtos.InviteResponse;
import com.swamisuite.tenant.repository.TenantInviteRepository;
import com.swamisuite.tenant.service.TenantService.TenantException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InviteService {

    private static final long INVITE_TTL_SECONDS = 7L * 24 * 3600;

    private final TenantInviteRepository inviteRepository;
    private final IdentityServiceClient identityServiceClient;
    private final SecureRandom random = new SecureRandom();

    public InviteService(TenantInviteRepository inviteRepository, IdentityServiceClient identityServiceClient) {
        this.inviteRepository = inviteRepository;
        this.identityServiceClient = identityServiceClient;
    }

    public InviteResponse invite(UUID tenantId, UUID invitedByUserId, String email, String roleName) {
        String rawToken = randomToken();
        TenantInvite invite = new TenantInvite(tenantId, email.toLowerCase(), roleName, hash(rawToken),
                invitedByUserId, Instant.now().plusSeconds(INVITE_TTL_SECONDS));
        invite = inviteRepository.save(invite);
        // Dev-mode: the invite link/token would normally be emailed once notification-service
        // exists; for Phase 1 the raw token is returned directly for the caller to share.
        return new InviteResponse(invite.getId(), invite.getEmail(), invite.getInvitedRoleName(),
                invite.getStatus().name(), invite.getExpiresAt(), rawToken);
    }

    public List<InviteResponse> listInvites(UUID tenantId) {
        return inviteRepository.findByTenantId(tenantId).stream()
                .map(i -> new InviteResponse(i.getId(), i.getEmail(), i.getInvitedRoleName(), i.getStatus().name(), i.getExpiresAt(), null))
                .toList();
    }

    public void revokeInvite(UUID tenantId, UUID inviteId) {
        TenantInvite invite = inviteRepository.findById(inviteId)
                .filter(i -> i.getTenantId().equals(tenantId))
                .orElseThrow(() -> new TenantException("Invite not found"));
        invite.setStatus(InviteStatus.REVOKED);
        inviteRepository.save(invite);
    }

    public EmployeeResponse acceptInvite(String rawToken, String password, String fullName) {
        TenantInvite invite = inviteRepository.findByTokenHash(hash(rawToken))
                .filter(TenantInvite::isAcceptable)
                .orElseThrow(() -> new TenantException("Invalid or expired invite"));

        UserSummary user = identityServiceClient.createUser(
                new CreateUserRequest(invite.getTenantId(), invite.getEmail(), password, fullName, invite.getInvitedRoleName()));

        invite.setStatus(InviteStatus.ACCEPTED);
        inviteRepository.save(invite);

        return new EmployeeResponse(user.id(), user.email(), user.fullName());
    }

    private String randomToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
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
