package com.swamisuite.subscription.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.subscription.dto.SubscriptionDtos.SubscriptionResponse;
import com.swamisuite.subscription.service.SubscriptionService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @GetMapping
    public List<SubscriptionResponse> list(Authentication authentication) {
        return subscriptionService.listForTenant(currentTenantId(authentication));
    }

    @PostMapping("/{id}/cancel")
    public void cancel(Authentication authentication, @PathVariable UUID id) {
        subscriptionService.cancel(currentTenantId(authentication), id);
    }

    private UUID currentTenantId(Authentication authentication) {
        String tenantId = ((JwtClaims) authentication.getPrincipal()).tenantId();
        if (tenantId == null) {
            throw new SubscriptionService.SubscriptionException("No tenant context");
        }
        return UUID.fromString(tenantId);
    }
}
