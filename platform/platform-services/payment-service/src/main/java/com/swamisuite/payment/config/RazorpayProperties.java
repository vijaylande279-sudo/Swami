package com.swamisuite.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Unset by default - real sandbox test-mode credentials go here via env vars once
 * obtained (see docs/adr/0002-payment-gateway-razorpay.md). Never committed.
 */
@ConfigurationProperties(prefix = "swamisuite.razorpay")
public record RazorpayProperties(String keyId, String keySecret, String webhookSecret) {
}
