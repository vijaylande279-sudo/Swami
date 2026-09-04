package com.swamisuite.subscription.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swamisuite.subscription.domain.Subscription;
import com.swamisuite.subscription.domain.Subscription.SubscriptionStatus;
import com.swamisuite.subscription.events.EntitlementEventPublisher;
import com.swamisuite.subscription.repository.EntitlementRepository;
import com.swamisuite.subscription.repository.SubscriptionRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private EntitlementRepository entitlementRepository;
    @Mock
    private CatalogServiceClient catalogServiceClient;
    @Mock
    private TenantServiceClient tenantServiceClient;
    @Mock
    private EntitlementEventPublisher eventPublisher;
    @Mock
    private EntitlementCacheService entitlementCacheService;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(subscriptionRepository, entitlementRepository,
                catalogServiceClient, tenantServiceClient, eventPublisher, entitlementCacheService);
    }

    @Test
    void createCheckoutIntent_rejectsWhenAlreadyActive() {
        UUID tenantId = UUID.randomUUID();
        Subscription active = new Subscription(tenantId, "restaurant", UUID.randomUUID());
        active.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findByTenantIdAndAppKey(tenantId, "restaurant")).thenReturn(Optional.of(active));

        assertThatThrownBy(() -> subscriptionService.createCheckoutIntent(tenantId, "restaurant"))
                .isInstanceOf(SubscriptionService.SubscriptionException.class);
    }

    @Test
    void activate_isIdempotent_doesNotRegrantOrRepublishWhenAlreadyActive() {
        UUID subscriptionId = UUID.randomUUID();
        Subscription subscription = new Subscription(UUID.randomUUID(), "restaurant", UUID.randomUUID());
        subscription.setId(subscriptionId);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        subscriptionService.activate(subscriptionId);

        verify(subscriptionRepository, never()).save(any());
        verify(eventPublisher, never()).publishEntitlementChanged(any(), any(), anyBoolean());
        verify(tenantServiceClient, never()).syncStatus(any(), any());
    }

    @Test
    void cancel_rejectsWhenSubscriptionBelongsToAnotherTenant() {
        UUID tenantAId = UUID.randomUUID();
        UUID tenantBId = UUID.randomUUID();
        UUID subscriptionId = UUID.randomUUID();
        Subscription tenantBSubscription = new Subscription(tenantBId, "restaurant", UUID.randomUUID());
        tenantBSubscription.setId(subscriptionId);
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(tenantBSubscription));

        assertThatThrownBy(() -> subscriptionService.cancel(tenantAId, subscriptionId))
                .isInstanceOf(SubscriptionService.SubscriptionException.class);
    }

    @Test
    void listForTenant_returnsOnlyThatTenantsSubscriptions() {
        UUID tenantId = UUID.randomUUID();
        Subscription s = new Subscription(tenantId, "restaurant", UUID.randomUUID());
        when(subscriptionRepository.findByTenantId(tenantId)).thenReturn(List.of(s));

        var result = subscriptionService.listForTenant(tenantId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).appKey()).isEqualTo("restaurant");
    }
}
