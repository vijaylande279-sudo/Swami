# 0004 — Stack versions for new platform code

## Status

Accepted

## Context

`PLATFORM_BUILD_INSTRUCTIONS.md` header specifies Java 21, Spring Boot 3.3+, Spring
Cloud, Angular 17+, and Bootstrap 5. The existing live app in this repo runs Java 17
(Spring Boot 3.3.4) on the backend, and Angular 21 with Tailwind CSS + Angular
Material + Signals-based state (no Bootstrap, no NgRx) on the frontend.

## Decision

- **Backend**: all new platform code (from Phase 0 onward) targets **Java 21** +
  Spring Boot 3.3+ + Spring Cloud, matching the doc. The existing `backend/` module
  stays on Java 17 until it is migrated in Phase 4, at which point it is upgraded to
  Java 21 to match the rest of the platform, as part of that phase's own plan.
- **Frontend**: keep the existing **Angular 21 + Tailwind CSS + Angular Material +
  Signals** stack for all frontend work, including the future `platform-shell` and
  per-app frontends. **Do not introduce Bootstrap 5.** This is an explicit deviation
  from the doc's stack line and its §8 mention of Bootstrap variable overrides per
  app theme.

## Rationale

- Java 21 is the current LTS and unlocks virtual threads and newer language features
  the doc's later phases assume are available; there is no reason to scaffold new
  services on Java 17.
- The existing frontend's Tailwind + Angular Material + Signals combination is
  already working, already themeable via Tailwind's token-based color configuration
  (`tailwind.config.js`'s `brand`/`status`/`surface` scales), and already free of a
  second state-management library. Introducing Bootstrap 5 alongside it would mean
  maintaining two competing styling systems for no functional gain — Tailwind can
  express the same per-app theming requirements in §8.2 (distinct palettes,
  typography, radii, shadows, motion) that the doc asks Bootstrap variable overrides
  to deliver.
- This decision was confirmed directly with the platform owner rather than assumed at
  the outset (per the doc's own Rule 0.6: "when something in this document is
  ambiguous, ask — do not guess and build").

## Consequences

- Every reference to "Bootstrap 5" elsewhere in `PLATFORM_BUILD_INSTRUCTIONS.md`
  (header, §8.2's "Bootstrap variable overrides") is superseded by this ADR: read it
  as "the app's own Tailwind theme + Angular Material theme overrides."
- `platform/pom.xml` pins `java.version=21` and a Spring Cloud release train
  compatible with Spring Boot 3.3.x (2023.0.x train) in `<dependencyManagement>`.
- No Bootstrap dependency is added to `frontend/package.json` at any phase unless a
  future ADR explicitly revisits this decision.
- Phase 4's restaurant-app migration plan must account for the Java 17 → 21 bump on
  the existing `backend/` module as one of its steps.
