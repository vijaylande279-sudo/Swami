-- Local read-model populated by consuming subscription-service's entitlement.changed
-- Kafka events - lets JwtIssuer include real entitlements without a synchronous
-- cross-service call on every login/refresh.
CREATE TABLE entitlement_grants (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id  UUID NOT NULL,
    app_key    VARCHAR(50) NOT NULL,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_entitlement_grants_tenant_app UNIQUE (tenant_id, app_key)
);

CREATE INDEX idx_entitlement_grants_tenant_id ON entitlement_grants (tenant_id);
