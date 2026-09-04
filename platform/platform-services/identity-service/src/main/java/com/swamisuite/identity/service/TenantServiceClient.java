package com.swamisuite.identity.service;

import com.swamisuite.identity.config.ServiceClientsConfig.ServiceUrlsProperties;
import com.swamisuite.identity.dto.InternalDtos.CreateTenantRequest;
import com.swamisuite.identity.dto.InternalDtos.TenantSummary;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls tenant-service's internal, non-gateway-routed API during signup. */
@Component
public class TenantServiceClient {

    private final RestClient restClient;
    private final ServiceUrlsProperties properties;

    public TenantServiceClient(RestClient tenantServiceRestClient, ServiceUrlsProperties properties) {
        this.restClient = tenantServiceRestClient;
        this.properties = properties;
    }

    public TenantSummary createTenant(String name, String primaryContactEmail) {
        return restClient.post()
                .uri("/internal/tenants")
                .header("X-Internal-Token", properties.internalToken())
                .body(new CreateTenantRequest(name, primaryContactEmail))
                .retrieve()
                .body(TenantSummary.class);
    }
}
