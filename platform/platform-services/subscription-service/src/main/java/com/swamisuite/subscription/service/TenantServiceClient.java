package com.swamisuite.subscription.service;

import com.swamisuite.subscription.config.ServiceClientsConfig.ServiceUrlsProperties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Syncs the coarse tenant-wide status rollup into tenant-service. Best-effort: a
 * failure here doesn't roll back the subscription transition (this service's own
 * Subscription row is the real source of truth) - a future reconciliation sweep
 * would re-sync any drift, noted as a known gap for this phase.
 */
@Component
public class TenantServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TenantServiceClient.class);

    private final RestClient restClient;
    private final ServiceUrlsProperties properties;

    public TenantServiceClient(RestClient tenantServiceRestClient, ServiceUrlsProperties properties) {
        this.restClient = tenantServiceRestClient;
        this.properties = properties;
    }

    public void syncStatus(UUID tenantId, String status) {
        try {
            restClient.patch()
                    .uri("/internal/tenants/{id}/status", tenantId)
                    .header("X-Internal-Token", properties.internalToken())
                    .body(new UpdateStatusRequest(status))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to sync tenant {} status to {} in tenant-service - will drift until reconciled: {}",
                    tenantId, status, e.getMessage());
        }
    }

    private record UpdateStatusRequest(String status) {
    }
}
