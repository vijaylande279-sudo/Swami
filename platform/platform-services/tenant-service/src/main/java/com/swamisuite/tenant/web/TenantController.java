package com.swamisuite.tenant.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.tenant.dto.TenantDtos.*;
import com.swamisuite.tenant.service.TenantService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:profile:read')")
    public TenantResponse get(Authentication authentication, @PathVariable UUID id) {
        return tenantService.getOwnTenant(currentTenantId(authentication), id);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('tenant:profile:update')")
    public TenantResponse update(Authentication authentication, @PathVariable UUID id,
                                  @RequestBody UpdateTenantRequest request) {
        return tenantService.updateOwnTenant(currentTenantId(authentication), id, request);
    }

    /** Manual lifecycle override for testing/support - platform-scoped permission, any tenant. */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('platform:tenant:status:update')")
    public TenantResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateStatusRequest request) {
        return tenantService.updateStatus(id, request.status());
    }

    static UUID currentTenantId(Authentication authentication) {
        String tenantId = ((JwtClaims) authentication.getPrincipal()).tenantId();
        if (tenantId == null) {
            throw new TenantService.TenantException("No tenant context");
        }
        return UUID.fromString(tenantId);
    }
}
