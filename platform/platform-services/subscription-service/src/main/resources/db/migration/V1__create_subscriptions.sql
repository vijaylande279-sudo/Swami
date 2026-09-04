CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE subscriptions (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id            UUID NOT NULL,
    app_key              VARCHAR(50) NOT NULL,
    plan_id              UUID NOT NULL,
    status               VARCHAR(20) NOT NULL DEFAULT 'PENDING_PAYMENT',
    current_period_start TIMESTAMPTZ,
    current_period_end   TIMESTAMPTZ,
    grace_until          TIMESTAMPTZ,
    razorpay_order_id    VARCHAR(100),
    cancelled_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at           TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uq_subscriptions_tenant_app UNIQUE (tenant_id, app_key)
);

CREATE INDEX idx_subscriptions_tenant_id ON subscriptions (tenant_id);
CREATE INDEX idx_subscriptions_status ON subscriptions (status);
