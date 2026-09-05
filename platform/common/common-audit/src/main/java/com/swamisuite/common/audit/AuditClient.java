package com.swamisuite.common.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;

/**
 * Posts a privileged action to audit-service. Fire-and-forget: a failure here is
 * logged loudly but never blocks or fails the caller's actual action - an audit
 * outage must not become a platform outage. Same shape as subscription-service's
 * TenantServiceClient.syncStatus from Phase 2.
 */
public class AuditClient {

    private static final Logger log = LoggerFactory.getLogger(AuditClient.class);

    private final RestClient restClient;
    private final String internalToken;

    public AuditClient(RestClient auditServiceRestClient, String internalToken) {
        this.restClient = auditServiceRestClient;
        this.internalToken = internalToken;
    }

    public void log(AuditEvent event) {
        try {
            restClient.post()
                    .uri("/internal/audit/events")
                    .header("X-Internal-Token", internalToken)
                    .body(event)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("Failed to record audit event ({} on {}/{}) - continuing without blocking the caller: {}",
                    event.action(), event.resourceType(), event.resourceId(), e.getMessage());
        }
    }
}
