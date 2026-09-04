CREATE TABLE tiers (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    app_id     UUID NOT NULL REFERENCES apps (id) ON DELETE CASCADE,
    key        VARCHAR(50) NOT NULL,
    name       VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_tiers_app_key UNIQUE (app_id, key)
);
