package com.swamisuite.tenant.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.tenant.dto.TenantDtos.*;
import com.swamisuite.tenant.service.InviteService;
import com.swamisuite.tenant.service.TenantService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class InviteController {

    private final InviteService inviteService;
    private final TenantService tenantService;

    public InviteController(InviteService inviteService, TenantService tenantService) {
        this.inviteService = inviteService;
        this.tenantService = tenantService;
    }

    @PostMapping("/tenants/{id}/invites")
    @PreAuthorize("hasAuthority('tenant:employee:invite')")
    public InviteResponse invite(Authentication authentication, @PathVariable UUID id,
                                  @Valid @RequestBody InviteEmployeeRequest request) {
        UUID callerTenantId = TenantController.currentTenantId(authentication);
        tenantService.requireOwned(callerTenantId, id);
        UUID callerUserId = UUID.fromString(((JwtClaims) authentication.getPrincipal()).subject());
        return inviteService.invite(id, callerUserId, request.email(), request.roleName());
    }

    @GetMapping("/tenants/{id}/invites")
    @PreAuthorize("hasAuthority('tenant:employee:read')")
    public List<InviteResponse> listInvites(Authentication authentication, @PathVariable UUID id) {
        tenantService.requireOwned(TenantController.currentTenantId(authentication), id);
        return inviteService.listInvites(id);
    }

    @DeleteMapping("/invites/{inviteId}")
    @PreAuthorize("hasAuthority('tenant:employee:revoke')")
    public void revoke(Authentication authentication, @PathVariable UUID inviteId) {
        inviteService.revokeInvite(TenantController.currentTenantId(authentication), inviteId);
    }

    /** Public - the invitee isn't authenticated yet; the invite token itself is the credential. */
    @PostMapping("/invites/{token}/accept")
    public EmployeeResponse accept(@PathVariable String token, @Valid @RequestBody AcceptInviteRequest request) {
        return inviteService.acceptInvite(token, request.password(), request.fullName());
    }
}
