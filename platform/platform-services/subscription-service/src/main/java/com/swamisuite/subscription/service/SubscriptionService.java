package com.swamisuite.subscription.service;

import com.swamisuite.subscription.domain.Entitlement;
import com.swamisuite.subscription.domain.Subscription;
import com.swamisuite.subscription.domain.Subscription.SubscriptionStatus;
import com.swamisuite.subscription.dto.SubscriptionDtos.CheckoutIntentResponse;
import com.swamisuite.subscription.dto.SubscriptionDtos.SubscriptionResponse;
import com.swamisuite.subscription.events.EntitlementEventPublisher;
import com.swamisuite.subscription.repository.EntitlementRepository;
import com.swamisuite.subscription.repository.SubscriptionRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private static final long ANNUAL_PERIOD_DAYS = 365;
    private static final long GRACE_PERIOD_DAYS = 7;

    private final SubscriptionRepository subscriptionRepository;
    private final EntitlementRepository entitlementRepository;
    private final CatalogServiceClient catalogServiceClient;
    private final TenantServiceClient tenantServiceClient;
    private final EntitlementEventPublisher eventPublisher;
    private final EntitlementCacheService entitlementCacheService;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, EntitlementRepository entitlementRepository,
                                CatalogServiceClient catalogServiceClient, TenantServiceClient tenantServiceClient,
                                EntitlementEventPublisher eventPublisher, EntitlementCacheService entitlementCacheService) {
        this.subscriptionRepository = subscriptionRepository;
        this.entitlementRepository = entitlementRepository;
        this.catalogServiceClient = catalogServiceClient;
        this.tenantServiceClient = tenantServiceClient;
        this.eventPublisher = eventPublisher;
        this.entitlementCacheService = entitlementCacheService;
    }

    /**
     * Server-computed checkout_intent - the caller (payment-service) never supplies
     * an amount; the price is always re-fetched here from catalog-service.
     */
    @Transactional
    public CheckoutIntentResponse createCheckoutIntent(UUID tenantId, String appKey) {
        Subscription existing = subscriptionRepository.findByTenantIdAndAppKey(tenantId, appKey).orElse(null);
        if (existing != null && existing.getStatus() == SubscriptionStatus.ACTIVE) {
            throw new SubscriptionException("Already subscribed to " + appKey);
        }

        var plan = catalogServiceClient.currentPlanForApp(appKey);
        Subscription subscription = existing != null ? existing : new Subscription(tenantId, appKey, plan.planId());
        subscription.setPlanId(plan.planId());
        subscription.setStatus(SubscriptionStatus.PENDING_PAYMENT);
        subscription = subscriptionRepository.save(subscription);

        return new CheckoutIntentResponse(subscription.getId(), plan.planId(), plan.pricePaise(), plan.gstPaise(), plan.totalPaise());
    }

    /** Called only by payment-service's signature-verified webhook handler. Idempotent - re-activating an already-ACTIVE subscription is a no-op. */
    @Transactional
    public SubscriptionResponse activate(UUID subscriptionId) {
        Subscription subscription = requireSubscription(subscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE) {
            return toResponse(subscription);
        }

        Instant now = Instant.now();
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCurrentPeriodStart(now);
        subscription.setCurrentPeriodEnd(now.plus(ANNUAL_PERIOD_DAYS, ChronoUnit.DAYS));
        subscription.setGraceUntil(null);
        subscription.setUpdatedAt(now);
        subscription = subscriptionRepository.save(subscription);

        grantEntitlement(subscription);
        return toResponse(subscription);
    }

    @Transactional
    public void cancel(UUID tenantId, UUID subscriptionId) {
        Subscription subscription = requireOwnedSubscription(tenantId, subscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        subscription.setCancelledAt(Instant.now());
        subscriptionRepository.save(subscription);
        revokeEntitlement(subscription);
    }

    public List<SubscriptionResponse> listForTenant(UUID tenantId) {
        return subscriptionRepository.findByTenantId(tenantId).stream().map(this::toResponse).toList();
    }

    /** Used by the suspension-sweep scheduled job. */
    @Transactional
    public void suspend(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.SUSPENDED);
        subscription.setUpdatedAt(Instant.now());
        subscriptionRepository.save(subscription);
        revokeEntitlement(subscription);
    }

    /** Used by the suspension-sweep job: period ended, entering the 7-day grace window. */
    @Transactional
    public void markPastDue(Subscription subscription) {
        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscription.setGraceUntil(subscription.getCurrentPeriodEnd().plus(GRACE_PERIOD_DAYS, ChronoUnit.DAYS));
        subscription.setUpdatedAt(Instant.now());
        subscriptionRepository.save(subscription);
        // Read access continues during grace per doc §6.1 - entitlement is not revoked yet.
    }

    private void grantEntitlement(Subscription subscription) {
        Entitlement entitlement = entitlementRepository
                .findByTenantIdAndAppKeyAndRevokedAtIsNull(subscription.getTenantId(), subscription.getAppKey())
                .orElseGet(() -> new Entitlement(subscription.getTenantId(), subscription.getAppKey(), subscription.getId()));
        entitlement.setSubscriptionId(subscription.getId());
        entitlementRepository.save(entitlement);

        refreshCacheAndPublish(subscription.getTenantId(), subscription.getAppKey(), true);
        syncTenantRollupStatus(subscription.getTenantId());
    }

    private void revokeEntitlement(Subscription subscription) {
        entitlementRepository.findByTenantIdAndAppKeyAndRevokedAtIsNull(subscription.getTenantId(), subscription.getAppKey())
                .ifPresent(e -> {
                    e.setRevokedAt(Instant.now());
                    entitlementRepository.save(e);
                });

        refreshCacheAndPublish(subscription.getTenantId(), subscription.getAppKey(), false);
        syncTenantRollupStatus(subscription.getTenantId());
    }

    private void refreshCacheAndPublish(UUID tenantId, String appKey, boolean granted) {
        List<String> activeAppKeys = entitlementRepository.findByTenantIdAndRevokedAtIsNull(tenantId).stream()
                .map(Entitlement::getAppKey).toList();
        entitlementCacheService.writeThrough(tenantId, activeAppKeys);
        eventPublisher.publishEntitlementChanged(tenantId, appKey, granted);
    }

    /** ACTIVE if any subscription is active, SUSPENDED only once none are. */
    private void syncTenantRollupStatus(UUID tenantId) {
        List<Subscription> subscriptions = subscriptionRepository.findByTenantId(tenantId);
        boolean anyActive = subscriptions.stream().anyMatch(s -> s.getStatus() == SubscriptionStatus.ACTIVE);
        String rollup = anyActive ? "ACTIVE" : "SUSPENDED";
        tenantServiceClient.syncStatus(tenantId, rollup);
    }

    private Subscription requireSubscription(UUID id) {
        return subscriptionRepository.findById(id).orElseThrow(() -> new SubscriptionException("Subscription not found"));
    }

    private Subscription requireOwnedSubscription(UUID tenantId, UUID id) {
        Subscription subscription = requireSubscription(id);
        if (!tenantId.equals(subscription.getTenantId())) {
            throw new SubscriptionException("Subscription not found");
        }
        return subscription;
    }

    private SubscriptionResponse toResponse(Subscription subscription) {
        return new SubscriptionResponse(subscription.getId(), subscription.getAppKey(), subscription.getStatus().name(),
                subscription.getCurrentPeriodStart(), subscription.getCurrentPeriodEnd());
    }

    public static class SubscriptionException extends RuntimeException {
        public SubscriptionException(String message) {
            super(message);
        }
    }
}
