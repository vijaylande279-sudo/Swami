-- razorpay_event_id's unique constraint IS the idempotency mechanism (doc §7.2/§15.7):
-- a duplicate delivery hits this constraint, is caught, and answered 200 as a no-op.
CREATE TABLE webhook_events (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    razorpay_event_id VARCHAR(100) NOT NULL UNIQUE,
    payload           TEXT NOT NULL,
    received_at       TIMESTAMPTZ NOT NULL DEFAULT now(),
    processed_at      TIMESTAMPTZ
);
