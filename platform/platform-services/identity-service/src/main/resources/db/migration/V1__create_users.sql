CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- email is globally unique (not per-tenant) for Phase 1: login is by email alone,
-- with no "pick your workspace" disambiguation step. A person who needs access to
-- two different tenants uses two different email addresses for now; a workspace
-- picker would be a reasonable later enhancement if that need turns out to matter.
CREATE TABLE users (
    id                    UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id             UUID,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    full_name             VARCHAR(255),
    status                VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    mfa_enabled           BOOLEAN NOT NULL DEFAULT FALSE,
    mfa_secret_encrypted  VARCHAR(255),
    created_at            TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_users_tenant_id ON users (tenant_id);
