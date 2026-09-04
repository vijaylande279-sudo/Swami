package com.swamisuite.payment.service;

import com.swamisuite.payment.config.ServiceClientsConfig.ServiceUrlsProperties;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class SubscriptionServiceClient {

    private final RestClient restClient;
    private final ServiceUrlsProperties properties;

    public SubscriptionServiceClient(RestClient subscriptionServiceRestClient, ServiceUrlsProperties properties) {
        this.restClient = subscriptionServiceRestClient;
        this.properties = properties;
    }

    public record CheckoutIntentRequest(UUID tenantId, String appKey) {
    }

    public record CheckoutIntentResponse(UUID subscriptionId, UUID planId, long amountPaise, long gstPaise, long totalPaise) {
    }

    /** The authoritative, server-computed price - payment-service never trusts a client-supplied amount. */
    public CheckoutIntentResponse createCheckoutIntent(UUID tenantId, String appKey) {
        return restClient.post()
                .uri("/internal/subscriptions/checkout-intent")
                .header("X-Internal-Token", properties.internalToken())
                .body(new CheckoutIntentRequest(tenantId, appKey))
                .retrieve()
                .body(CheckoutIntentResponse.class);
    }

    /** Called only after the webhook signature has been verified. */
    public void activate(UUID subscriptionId) {
        restClient.post()
                .uri("/internal/subscriptions/{id}/activate", subscriptionId)
                .header("X-Internal-Token", properties.internalToken())
                .retrieve()
                .toBodilessEntity();
    }
}
