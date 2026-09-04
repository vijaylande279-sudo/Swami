package com.swamisuite.tenant.service;

import com.swamisuite.tenant.domain.Tenant;
import com.swamisuite.tenant.domain.Tenant.TenantStatus;
import com.swamisuite.tenant.dto.TenantDtos.TenantResponse;
import com.swamisuite.tenant.dto.TenantDtos.UpdateTenantRequest;
import com.swamisuite.tenant.repository.TenantRepository;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public TenantResponse createTenant(String name, String primaryContactEmail) {
        Tenant tenant = new Tenant(name, uniqueSlug(name), primaryContactEmail);
        return toResponse(tenantRepository.save(tenant));
    }

    /** Enforces that the caller may only ever see/act on their own tenant (cross-tenant isolation gate). */
    public TenantResponse getOwnTenant(UUID callerTenantId, UUID requestedTenantId) {
        Tenant tenant = requireOwned(callerTenantId, requestedTenantId);
        return toResponse(tenant);
    }

    public TenantResponse updateOwnTenant(UUID callerTenantId, UUID requestedTenantId, UpdateTenantRequest request) {
        Tenant tenant = requireOwned(callerTenantId, requestedTenantId);
        if (request.name() != null) tenant.setName(request.name());
        if (request.gstin() != null) tenant.setGstin(request.gstin());
        if (request.primaryContactPhone() != null) tenant.setPrimaryContactPhone(request.primaryContactPhone());
        tenant.setUpdatedAt(Instant.now());
        return toResponse(tenantRepository.save(tenant));
    }

    /** Support/super-admin only - not subject to the same-tenant restriction, gated by @PreAuthorize instead. */
    public TenantResponse updateStatus(UUID tenantId, String status) {
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new TenantException("Tenant not found"));
        tenant.setStatus(TenantStatus.valueOf(status.toUpperCase(Locale.ROOT)));
        tenant.setUpdatedAt(Instant.now());
        return toResponse(tenantRepository.save(tenant));
    }

    public Tenant requireOwned(UUID callerTenantId, UUID requestedTenantId) {
        if (callerTenantId == null || !callerTenantId.equals(requestedTenantId)) {
            // Never distinguish "not found" from "not yours" - both look identical to the caller.
            throw new TenantException("Tenant not found");
        }
        return tenantRepository.findById(requestedTenantId).orElseThrow(() -> new TenantException("Tenant not found"));
    }

    private String uniqueSlug(String name) {
        String base = name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) {
            base = "tenant";
        }
        String candidate = base;
        int suffix = 1;
        while (tenantRepository.existsBySlug(candidate)) {
            candidate = base + "-" + (++suffix);
        }
        return candidate;
    }

    private TenantResponse toResponse(Tenant tenant) {
        return new TenantResponse(tenant.getId(), tenant.getName(), tenant.getSlug(), tenant.getGstin(),
                tenant.getPrimaryContactEmail(), tenant.getPrimaryContactPhone(),
                tenant.getStatus().name(), tenant.getTrialEndsAt());
    }

    public static class TenantException extends RuntimeException {
        public TenantException(String message) {
            super(message);
        }
    }
}
