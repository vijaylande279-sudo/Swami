CREATE TABLE mfa_backup_codes (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id   UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    code_hash VARCHAR(255) NOT NULL,
    used_at   TIMESTAMPTZ
);

CREATE INDEX idx_mfa_backup_codes_user_id ON mfa_backup_codes (user_id);
