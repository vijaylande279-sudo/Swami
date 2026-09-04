package com.swamisuite.tenant.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Table(name = "tenant_invites")
@Getter
@Setter
@NoArgsConstructor
public class TenantInvite {

    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "tenant_id", nullable = false, columnDefinition = "uuid")
    private UUID tenantId;

    @Column(nullable = false)
    private String email;

    /** Role name to grant on acceptance, e.g. TENANT_MANAGER or a custom tenant role name. */
    @Column(name = "invited_role_name", nullable = false)
    private String invitedRoleName;

    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "invited_by_user_id", columnDefinition = "uuid")
    private UUID invitedByUserId;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public TenantInvite(UUID tenantId, String email, String invitedRoleName, String tokenHash,
                         UUID invitedByUserId, Instant expiresAt) {
        this.tenantId = tenantId;
        this.email = email;
        this.invitedRoleName = invitedRoleName;
        this.tokenHash = tokenHash;
        this.invitedByUserId = invitedByUserId;
        this.expiresAt = expiresAt;
    }

    public boolean isAcceptable() {
        return status == InviteStatus.PENDING && Instant.now().isBefore(expiresAt);
    }

    public enum InviteStatus {
        PENDING, ACCEPTED, EXPIRED, REVOKED
    }
}
