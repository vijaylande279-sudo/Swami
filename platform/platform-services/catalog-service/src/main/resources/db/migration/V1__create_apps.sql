CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE apps (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    key         VARCHAR(50) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);
