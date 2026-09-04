CREATE TABLE refunds (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    checkout_intent_id  UUID NOT NULL REFERENCES checkout_intents (id),
    razorpay_refund_id  VARCHAR(100),
    amount_paise        BIGINT NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'INITIATED',
    reason              VARCHAR(500) NOT NULL,
    initiated_by        UUID NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);
