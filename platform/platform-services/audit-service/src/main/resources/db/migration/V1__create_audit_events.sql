CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Append-only. No application code path ever updates or deletes a row here
-- (AuditEventRepository deliberately doesn't extend JpaRepository), per doc §12.
CREATE TABLE audit_events (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_id      UUID,
    actor_type    VARCHAR(20) NOT NULL,
    tenant_id     UUID,
    action        VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id   VARCHAR(100),
    metadata      JSONB,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_events_tenant_id ON audit_events (tenant_id);
CREATE INDEX idx_audit_events_action ON audit_events (action);
CREATE INDEX idx_audit_events_created_at ON audit_events (created_at);
