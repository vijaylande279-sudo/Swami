CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE checkout_intents (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL,
    subscription_id   UUID NOT NULL,
    app_key           VARCHAR(50) NOT NULL,
    amount_paise      BIGINT NOT NULL,
    gst_paise         BIGINT NOT NULL,
    total_paise       BIGINT NOT NULL,
    razorpay_order_id   VARCHAR(100),
    razorpay_payment_id VARCHAR(100),
    status            VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_checkout_intents_tenant_id ON checkout_intents (tenant_id);
CREATE UNIQUE INDEX uq_checkout_intents_razorpay_order_id ON checkout_intents (razorpay_order_id) WHERE razorpay_order_id IS NOT NULL;
