package com.swamisuite.payment.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.swamisuite.payment.config.RazorpayProperties;
import java.nio.charset.StandardCharsets;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;

/** Proves the doc's non-negotiable: webhook signature verification with a real HMAC-SHA256 computation, not a hand-waved comparison. */
class WebhookSignatureVerifierTest {

    private static final String SECRET = "test-webhook-secret";
    private final RazorpayProperties properties = new RazorpayProperties("key", "secret", SECRET);
    private final WebhookSignatureVerifier verifier = new WebhookSignatureVerifier(properties);

    @Test
    void isValid_acceptsACorrectlySignedPayload() throws Exception {
        String payload = "{\"event\":\"payment.captured\"}";
        String signature = computeSignature(payload, SECRET);

        assertThat(verifier.isValid(payload, signature)).isTrue();
    }

    @Test
    void isValid_rejectsATamperedPayload() throws Exception {
        String originalPayload = "{\"event\":\"payment.captured\",\"amount\":100}";
        String signature = computeSignature(originalPayload, SECRET);
        String tamperedPayload = "{\"event\":\"payment.captured\",\"amount\":999999}";

        assertThat(verifier.isValid(tamperedPayload, signature)).isFalse();
    }

    @Test
    void isValid_rejectsASignatureComputedWithTheWrongSecret() throws Exception {
        String payload = "{\"event\":\"payment.captured\"}";
        String signature = computeSignature(payload, "wrong-secret");

        assertThat(verifier.isValid(payload, signature)).isFalse();
    }

    @Test
    void isValid_rejectsWhenNoSignatureProvided() {
        assertThat(verifier.isValid("{}", null)).isFalse();
    }

    @Test
    void isValid_rejectsWhenNoWebhookSecretConfigured() {
        var unconfigured = new WebhookSignatureVerifier(new RazorpayProperties("key", "secret", null));
        assertThat(unconfigured.isValid("{}", "anything")).isFalse();
    }

    private String computeSignature(String payload, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
