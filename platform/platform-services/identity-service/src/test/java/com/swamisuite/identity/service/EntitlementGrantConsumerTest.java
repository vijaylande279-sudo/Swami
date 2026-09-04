package com.swamisuite.identity.service;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.swamisuite.common.events.EntitlementChangedEvent;
import com.swamisuite.identity.domain.EntitlementGrant;
import com.swamisuite.identity.repository.EntitlementGrantRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EntitlementGrantConsumerTest {

    @Mock
    private EntitlementGrantRepository entitlementGrantRepository;

    @Test
    void onEntitlementChanged_grantedTwice_doesNotCreateDuplicateRows() {
        var consumer = new EntitlementGrantConsumer(entitlementGrantRepository);
        UUID tenantId = UUID.randomUUID();
        var event = new EntitlementChangedEvent(UUID.randomUUID(), tenantId.toString(), Instant.now(), "restaurant", true);

        when(entitlementGrantRepository.findByTenantIdAndAppKey(tenantId, "restaurant"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new EntitlementGrant(tenantId, "restaurant")));

        consumer.onEntitlementChanged(event);
        consumer.onEntitlementChanged(event);

        verify(entitlementGrantRepository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    void onEntitlementChanged_revoked_deletesTheGrant() {
        var consumer = new EntitlementGrantConsumer(entitlementGrantRepository);
        UUID tenantId = UUID.randomUUID();
        var event = new EntitlementChangedEvent(UUID.randomUUID(), tenantId.toString(), Instant.now(), "restaurant", false);

        consumer.onEntitlementChanged(event);

        verify(entitlementGrantRepository).deleteByTenantIdAndAppKey(tenantId, "restaurant");
        verify(entitlementGrantRepository, never()).save(ArgumentMatchers.any());
    }
}
