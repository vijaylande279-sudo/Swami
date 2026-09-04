package com.swamisuite.tenant.web;

import com.swamisuite.tenant.dto.InternalDtos.CreateTenantRequest;
import com.swamisuite.tenant.dto.InternalDtos.TenantSummary;
import com.swamisuite.tenant.dto.InternalDtos.UpdateTenantStatusRequest;
import com.swamisuite.tenant.service.TenantService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service only - guarded by InternalTokenFilter, never routed through the gateway. */
@RestController
@RequestMapping("/internal")
public class InternalTenantController {

    private final TenantService tenantService;

    public InternalTenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping("/tenants")
    public TenantSummary createTenant(@Valid @RequestBody CreateTenantRequest request) {
        var tenant = tenantService.createTenant(request.name(), request.primaryContactEmail());
        return new TenantSummary(tenant.id(), tenant.name(), tenant.slug(), tenant.status());
    }

    /**
     * subscription-service's status-rollup sync target - no user JWT exists at the
     * point a webhook handler or scheduled job needs to update tenant status, so
     * this can't reuse the human-support /tenants/{id}/status endpoint.
     */
    @PatchMapping("/tenants/{id}/status")
    public void updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateTenantStatusRequest request) {
        tenantService.updateStatus(id, request.status());
    }
}
