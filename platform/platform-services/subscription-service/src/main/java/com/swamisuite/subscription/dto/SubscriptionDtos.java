package com.swamisuite.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public final class SubscriptionDtos {

    private SubscriptionDtos() {
    }

    public record CheckoutIntentRequest(UUID tenantId, @NotBlank String appKey) {
    }

    public record CheckoutIntentResponse(UUID subscriptionId, UUID planId, long amountPaise, long gstPaise, long totalPaise) {
    }

    public record SubscriptionResponse(UUID id, String appKey, String status,
                                        Instant currentPeriodStart, Instant currentPeriodEnd) {
    }
}
