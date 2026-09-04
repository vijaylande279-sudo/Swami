CREATE TABLE plans (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tier_id          UUID NOT NULL REFERENCES tiers (id) ON DELETE CASCADE,
    plan_key         VARCHAR(100) NOT NULL,
    billing_interval VARCHAR(20) NOT NULL DEFAULT 'ANNUAL',
    price_paise      BIGINT NOT NULL,
    gst_rate_bps     INT NOT NULL,
    effective_from   TIMESTAMPTZ NOT NULL DEFAULT now(),
    effective_to     TIMESTAMPTZ
);

CREATE INDEX idx_plans_tier_id ON plans (tier_id);
