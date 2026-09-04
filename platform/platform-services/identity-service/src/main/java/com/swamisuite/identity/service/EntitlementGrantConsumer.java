package com.swamisuite.identity.service;

import com.swamisuite.common.events.EntitlementChangedEvent;
import com.swamisuite.identity.domain.EntitlementGrant;
import com.swamisuite.identity.repository.EntitlementGrantRepository;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Keeps entitlement_grants in sync with subscription-service's entitlement.changed events. */
@Service
public class EntitlementGrantConsumer {

    private static final Logger log = LoggerFactory.getLogger(EntitlementGrantConsumer.class);

    private final EntitlementGrantRepository entitlementGrantRepository;

    public EntitlementGrantConsumer(EntitlementGrantRepository entitlementGrantRepository) {
        this.entitlementGrantRepository = entitlementGrantRepository;
    }

    @KafkaListener(topics = EntitlementChangedEvent.TOPIC, containerFactory = "entitlementChangedListenerContainerFactory")
    @Transactional
    public void onEntitlementChanged(EntitlementChangedEvent event) {
        UUID tenantId = UUID.fromString(event.getTenantId());

        if (event.isGranted()) {
            if (entitlementGrantRepository.findByTenantIdAndAppKey(tenantId, event.getAppKey()).isEmpty()) {
                entitlementGrantRepository.save(new EntitlementGrant(tenantId, event.getAppKey()));
            }
        } else {
            entitlementGrantRepository.deleteByTenantIdAndAppKey(tenantId, event.getAppKey());
        }

        log.info("Synced entitlement grant: tenant={} app={} granted={}", tenantId, event.getAppKey(), event.isGranted());
    }
}
