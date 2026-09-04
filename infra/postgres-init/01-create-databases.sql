-- Runs once, only against a fresh (empty) postgres data volume, per Postgres's own
-- /docker-entrypoint-initdb.d/ mechanism. Append new CREATE DATABASE lines here as
-- later phases add catalog-service, subscription-service, etc.
CREATE DATABASE db_identity;
CREATE DATABASE db_tenant;
