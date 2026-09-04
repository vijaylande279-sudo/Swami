package com.swamisuite.tenant.web;

import com.swamisuite.tenant.dto.InternalDtos.UserSummary;
import com.swamisuite.tenant.dto.TenantDtos.EmployeeResponse;
import com.swamisuite.tenant.service.IdentityServiceClient;
import com.swamisuite.tenant.service.TenantService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmployeeController {

    private final IdentityServiceClient identityServiceClient;
    private final TenantService tenantService;

    public EmployeeController(IdentityServiceClient identityServiceClient, TenantService tenantService) {
        this.identityServiceClient = identityServiceClient;
        this.tenantService = tenantService;
    }

    @GetMapping("/tenants/{id}/employees")
    @PreAuthorize("hasAuthority('tenant:employee:read')")
    public List<EmployeeResponse> listEmployees(Authentication authentication, @PathVariable UUID id) {
        tenantService.requireOwned(TenantController.currentTenantId(authentication), id);
        return identityServiceClient.listByTenant(id).stream()
                .map(this::toEmployeeResponse)
                .toList();
    }

    private EmployeeResponse toEmployeeResponse(UserSummary user) {
        return new EmployeeResponse(user.id(), user.email(), user.fullName());
    }
}
