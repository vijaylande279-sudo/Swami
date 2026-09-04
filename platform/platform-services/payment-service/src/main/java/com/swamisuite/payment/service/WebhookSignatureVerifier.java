package com.swamisuite.payment.service;

import com.swamisuite.payment.config.RazorpayProperties;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Component;

/**
 * HMAC-SHA256 verification of Razorpay's X-Razorpay-Signature header, per doc
 * §7.2/§15.7 - constant-time comparison via MessageDigest.isEqual, never
 * String.equals. This is the ONLY thing that authenticates POST /payments/webhook;
 * it deliberately has no other auth (no JWT, no internal token) since Razorpay calls
 * it directly from the internet.
 */
@Component
public class WebhookSignatureVerifier {

    private static final String HMAC_SHA256 = "HmacSHA256";

    private final RazorpayProperties properties;

    public WebhookSignatureVerifier(RazorpayProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(String rawPayload, String providedSignature) {
        if (providedSignature == null || properties.webhookSecret() == null || properties.webhookSecret().isBlank()) {
            return false;
        }
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(properties.webhookSecret().getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] computed = mac.doFinal(rawPayload.getBytes(StandardCharsets.UTF_8));
            String computedHex = toHex(computed);
            return MessageDigest.isEqual(computedHex.getBytes(StandardCharsets.UTF_8), providedSignature.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC-SHA256 unavailable", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        return sb.toString();
    }
}
