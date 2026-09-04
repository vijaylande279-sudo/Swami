# 0003 — Monorepo directory strategy for Phase 0

## Status

Accepted

## Context

`c:\Swami` already contains a live, single-tenant restaurant application deployed to
Railway: a single Spring Boot module at `backend/` and a single Angular workspace at
`frontend/`. `PLATFORM_BUILD_INSTRUCTIONS.md` §10 specifies a target repo layout where
`backend/` itself becomes the multi-module Maven reactor root (`backend/pom.xml`,
`backend/common/*`, `backend/platform/*`, `backend/apps/*`), with the current
restaurant app's logic migrated into `backend/apps/restaurant-service/`.

Per the build instructions' Rule 3 ("one phase at a time") and Rule 2 ("never move from
investigation to implementation without approval, especially anything touching an
existing running service"), Phase 0 must not touch `backend/` or `frontend/` at all —
that migration is explicitly Phase 4 (§11). But Phase 0 still needs to create the
parent POM, common modules, and platform-services skeleton somewhere.

## Decision

Create a **new top-level directory `platform/`** as the multi-module Maven reactor
root for Phase 0, structured as:

```
platform/
├── pom.xml
├── common/
│   ├── common-security/
│   ├── common-events/
│   ├── common-web/
│   └── common-tenancy/
└── platform-services/
    ├── discovery-server/
    ├── config-server/
    ├── api-gateway/
    └── hello-service/
```

`platform/platform-services/` (rather than a nested `platform/platform/`) is used to
avoid a confusing doubled directory name, since the doc's §10 layout nests its
`platform/` services layer *inside* `backend/`, and our reactor root is itself named
`platform/`.

## Rationale

- Zero risk to the live, currently-deployed `backend/`/`frontend/` apps during Phases
  0–3.5: nothing in this plan reads, writes, or references those directories.
- `platform/` closely matches the doc's own internal naming for this layer of
  services, minimizing the mental translation needed when reading
  `PLATFORM_BUILD_INSTRUCTIONS.md` alongside the actual repo.
- Keeps the eventual Phase 4 move mechanical rather than a redesign (see Consequences).

## Consequences

In Phase 4, when the existing restaurant app is migrated:

1. `backend/` (current single-module restaurant app) is moved via `git mv` into
   `platform/apps/restaurant-service/`, and its package is migrated from
   `com.hotel.oms` to `com.swamisuite.restaurant` as part of that phase's own plan.
2. `platform/platform-services/` is renamed to `platform/platform/` via `git mv`, to
   match the doc's literal §10 layout exactly.
3. At that point `platform/` fully matches the doc's target `backend/` layout in
   substance (parent POM + `common/` + `platform/` + `apps/`), just under a directory
   named `platform/` instead of `backend/`. Whether to also rename the top-level
   directory from `platform/` to `backend/` (retiring the old top-level `backend/`
   name entirely) is a decision for the Phase 4 plan, not this one.
4. `frontend/` restructuring into the doc's `projects/`/`libs/` Angular workspace
   layout (§8.1) is out of scope for both Phase 0 and Phase 4 per the phase list —
   it begins alongside the restaurant frontend migration and continues through
   Phase 5.
