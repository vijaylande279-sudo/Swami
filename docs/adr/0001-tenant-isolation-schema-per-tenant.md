# 0001 — Tenant isolation: schema-per-tenant per application database

## Status

Accepted

## Context

Swami Suite is a multi-tenant SaaS platform. Each vertical application (restaurant,
coffee-shop, hotel, bar-restro) has its own dedicated database (`db_restaurant`,
`db_coffeeshop`, `db_hotel`, `db_barrestro`), per `PLATFORM_BUILD_INSTRUCTIONS.md` §5.
Within each application database we still need to isolate one tenant's rows from
another's. Three standard options exist:

1. **Row-level isolation** — a single shared schema per app database, every table
   carries a `tenant_id` column, and every query filters on it.
2. **Schema-per-tenant** — one database per app, one Postgres schema per tenant inside
   it, each schema holding the tenant's own copy of the application's tables.
3. **Database-per-tenant** — a fully separate database (not just schema) per tenant per
   app.

## Decision

Use **schema-per-tenant** inside each application's database, per
`PLATFORM_BUILD_INSTRUCTIONS.md` §5. Platform services that are inherently
cross-tenant by nature (`tenant-service`, `subscription-service`, `payment-service`)
remain shared-schema with a `tenant_id` column and a mandatory Hibernate `@Filter`,
since their entire job is to reason across tenants.

## Rationale

- **Isolation vs. row-level**: a bug in a `WHERE tenant_id = ?` clause is a data leak
  across paying customers; a missing schema context fails closed (wrong/no schema,
  not another tenant's schema) far more often than it fails open. Given the platform's
  entire pitch rests on "your data is never visible to another business" (§16's DPDP Act
  discussion), the stronger default is worth the operational cost.
- **Cost vs. database-per-tenant**: a full database per tenant per app multiplies
  connection pools, credentials, and backup/restore units by tenant count — unworkable
  at a few hundred tenants across four apps. Schema-per-tenant keeps one connection
  pool and one set of credentials per app database while still giving each tenant
  physically separate tables.
- **Migration story**: Flyway supports per-schema migration via `flyway.schemas`,
  making it straightforward to provision a new tenant's schema by replaying the same
  migration set against a new schema name.

## Consequences

- `common-tenancy` (this repo, `platform/common/common-tenancy`) must provide a
  Hibernate `CurrentTenantIdentifierResolver` and `MultiTenantConnectionProvider`,
  driven by a `TenantContext` populated from the JWT tenant claim in a servlet filter.
  Phase 0 delivers the interfaces and a default-schema stub only; real per-tenant
  DataSource routing is Phase 1+.
- Tenant schema provisioning must be asynchronous, idempotent, and retryable
  (triggered by a `subscription.activated` event in later phases).
- A release gate is required (per §5): an integration test proving tenant A cannot
  read tenant B's rows through any endpoint.
- Set a review trigger at ~200 tenants per app to reassess whether schema-per-tenant
  is still manageable, per the risk register in §14 of the build instructions.
