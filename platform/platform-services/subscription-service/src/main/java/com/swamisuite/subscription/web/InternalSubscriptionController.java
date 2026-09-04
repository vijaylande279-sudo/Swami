package com.swamisuite.subscription.web;

import com.swamisuite.subscription.dto.SubscriptionDtos.CheckoutIntentRequest;
import com.swamisuite.subscription.dto.SubscriptionDtos.CheckoutIntentResponse;
import com.swamisuite.subscription.dto.SubscriptionDtos.SubscriptionResponse;
import com.swamisuite.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Service-to-service only - guarded by InternalTokenFilter, never routed through the gateway. Called by payment-service. */
@RestController
@RequestMapping("/internal/subscriptions")
public class InternalSubscriptionController {

    private final SubscriptionService subscriptionService;

    public InternalSubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/checkout-intent")
    public CheckoutIntentResponse checkoutIntent(@Valid @RequestBody CheckoutIntentRequest request) {
        return subscriptionService.createCheckoutIntent(request.tenantId(), request.appKey());
    }

    /** Called only after payment-service has verified the Razorpay webhook signature. */
    @PostMapping("/{id}/activate")
    public SubscriptionResponse activate(@PathVariable UUID id) {
        return subscriptionService.activate(id);
    }
}
