CREATE TABLE entitlements (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id       UUID NOT NULL,
    app_key         VARCHAR(50) NOT NULL,
    subscription_id UUID NOT NULL REFERENCES subscriptions (id) ON DELETE CASCADE,
    granted_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
    revoked_at      TIMESTAMPTZ
);

CREATE INDEX idx_entitlements_tenant_id ON entitlements (tenant_id);
CREATE UNIQUE INDEX uq_entitlements_active_tenant_app ON entitlements (tenant_id, app_key) WHERE revoked_at IS NULL;
