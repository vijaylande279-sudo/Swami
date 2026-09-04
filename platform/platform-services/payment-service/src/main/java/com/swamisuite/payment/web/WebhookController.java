package com.swamisuite.payment.web;

import com.swamisuite.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * Publicly reachable (Razorpay calls this from the internet) - added to the
 * gateway's public-path allowlist. Security is entirely the signature check, not
 * JWT/internal-token, per doc §7.2/§15.1.
 */
@RestController
public class WebhookController {

    private final PaymentService paymentService;

    public WebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/payments/webhook")
    public ResponseEntity<Void> handleWebhook(@RequestBody String rawPayload,
                                               @RequestHeader("X-Razorpay-Signature") String signature) {
        // Used as the idempotency key too: HMAC-SHA256 is deterministic over
        // (payload, secret), so an identical redelivery produces an identical
        // signature, and a genuinely different event produces a different one -
        // this holds regardless of which header names a given Razorpay API version
        // sends for an explicit event id.
        paymentService.handleWebhook(rawPayload, signature, signature);
        return ResponseEntity.ok().build();
    }
}
