package com.swamisuite.tenant.service;

import com.swamisuite.tenant.config.ServiceClientsConfig.ServiceUrlsProperties;
import com.swamisuite.tenant.dto.InternalDtos.CreateUserRequest;
import com.swamisuite.tenant.dto.InternalDtos.UserSummary;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/** Calls identity-service's internal, non-gateway-routed API to provision an invited employee's account. */
@Component
public class IdentityServiceClient {

    private final RestClient restClient;
    private final ServiceUrlsProperties properties;

    public IdentityServiceClient(RestClient identityServiceRestClient, ServiceUrlsProperties properties) {
        this.restClient = identityServiceRestClient;
        this.properties = properties;
    }

    public UserSummary createUser(CreateUserRequest request) {
        return restClient.post()
                .uri("/internal/users")
                .header("X-Internal-Token", properties.internalToken())
                .body(request)
                .retrieve()
                .body(UserSummary.class);
    }

    public List<UserSummary> listByTenant(UUID tenantId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/internal/users").queryParam("tenantId", tenantId).build())
                .header("X-Internal-Token", properties.internalToken())
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<UserSummary>>() {
                });
    }
}
