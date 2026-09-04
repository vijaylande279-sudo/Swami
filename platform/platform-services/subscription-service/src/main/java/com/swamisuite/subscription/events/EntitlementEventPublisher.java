package com.swamisuite.subscription.events;

import com.swamisuite.common.events.DomainEvent;
import com.swamisuite.common.events.EntitlementChangedEvent;
import java.time.Instant;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * subscription-service is the sole producer of entitlement.changed - identity-service
 * consumes it to keep its local entitlement read-model in sync for JWT issuance.
 *
 * <p>Published after the DB commit that changed the entitlement, not inside the same
 * transaction - an at-least-once, no-outbox simplification for this phase (a message
 * could theoretically be lost if the process crashes between commit and publish;
 * acceptable given the cache/JWT claim this feeds is already TTL-bounded and
 * recoverable via the reconciliation sweep noted in TenantServiceClient).
 */
@Component
public class EntitlementEventPublisher {

    private final KafkaTemplate<String, DomainEvent> kafkaTemplate;

    public EntitlementEventPublisher(KafkaTemplate<String, DomainEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishEntitlementChanged(UUID tenantId, String appKey, boolean granted) {
        EntitlementChangedEvent event = new EntitlementChangedEvent(
                UUID.randomUUID(), tenantId.toString(), Instant.now(), appKey, granted);
        kafkaTemplate.send(EntitlementChangedEvent.TOPIC, tenantId.toString(), event);
    }
}
