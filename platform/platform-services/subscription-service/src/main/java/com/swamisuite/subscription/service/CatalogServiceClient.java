package com.swamisuite.subscription.service;

import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class CatalogServiceClient {

    private final RestClient restClient;

    public CatalogServiceClient(RestClient catalogServiceRestClient) {
        this.restClient = catalogServiceRestClient;
    }

    public record PlanSummary(UUID planId, String planKey, String billingInterval,
                               long pricePaise, long gstPaise, long totalPaise) {
    }

    /**
     * The authoritative price re-fetch that backs the server-computed checkout_intent
     * - never trusts any caller-supplied amount. This calls catalog-service's public
     * /catalog/apps/{appKey}/plan (the same endpoint the frontend's pricing page
     * reads) rather than an internal one, since it's read-only public catalog data.
     */
    public PlanSummary currentPlanForApp(String appKey) {
        return restClient.get()
                .uri("/catalog/apps/{appKey}/plan", appKey)
                .retrieve()
                .body(PlanSummary.class);
    }
}
