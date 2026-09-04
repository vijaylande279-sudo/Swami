CREATE TABLE tenant_invites (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id         UUID NOT NULL REFERENCES tenants (id) ON DELETE CASCADE,
    email             VARCHAR(255) NOT NULL,
    invited_role_name VARCHAR(100) NOT NULL,
    token_hash        VARCHAR(255) NOT NULL UNIQUE,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    invited_by_user_id UUID,
    expires_at        TIMESTAMPTZ NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_tenant_invites_tenant_id ON tenant_invites (tenant_id);
