CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tenants (
    id                     UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                   VARCHAR(255) NOT NULL,
    slug                   VARCHAR(255) NOT NULL UNIQUE,
    gstin                  VARCHAR(20),
    primary_contact_email  VARCHAR(255) NOT NULL,
    primary_contact_phone  VARCHAR(30),
    status                 VARCHAR(20) NOT NULL DEFAULT 'TRIALING',
    trial_ends_at          TIMESTAMPTZ,
    created_at             TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at             TIMESTAMPTZ NOT NULL DEFAULT now()
);
