# 0002 — Payment gateway: Razorpay

## Status

Accepted

## Context

`PLATFORM_BUILD_INSTRUCTIONS.md` §2 (D1) and §7 propose Razorpay as the payment
gateway, with Stripe as a fallback if non-INR billing is needed later. All pricing in
the platform (§1, §15) is INR-denominated, GST-inclusive, and targets Indian SMB
customers (restaurants, coffee shops, hotels, bars).

## Decision

Use **Razorpay** as the sole payment gateway for the initial build (Phase 2 onward).

## Rationale

- INR-native with UPI, cards, netbanking and wallets supported out of the box —
  covers the primary payment methods for the target customer base (§15.3, §15.4).
- Has a Subscriptions API and NACH e-mandate support, both required for the
  recurring-billing and enterprise-tier flows in §15.5.
- Has a maintained Java SDK, consistent with the Spring Boot backend.
- Hosted checkout (iframe) keeps card/UPI credential capture entirely outside our
  infrastructure, satisfying the PCI posture in §15.1 ("we never see a card number,
  a CVV, or a UPI PIN") without building our own PCI-scoped card form.

## Consequences

- `payment-service` (later phase) implements Razorpay order/subscription creation,
  webhook signature verification (`X-Razorpay-Signature`, HMAC-SHA256), and the
  `checkout_intent` / `payment_mandate` data model described in §15.8.
- The webhook — not the browser redirect — is the sole source of truth for granting
  access (§7.2, §15.1). This must be enforced in `payment-service` when it is built.
- Gateway API keys and webhook secret live in the config server / vault, never in
  `application.yml` in git, with separate sandbox/live credential sets selected by
  Spring profile (§15.6). A build must fail to start if a live key is present in a
  non-prod profile — this check is implemented when `payment-service` is scaffolded,
  not in Phase 0.
- Re-verify the ₹15,000 e-mandate no-AFA ceiling with Razorpay at the start of Phase 2
  (§15.5) — it has been revised before and will be revised again.
- If a non-INR market is needed later, Stripe (or another gateway) can be added behind
  the same `payment-service` boundary without touching other services, since payment
  concerns are isolated to that one service.
