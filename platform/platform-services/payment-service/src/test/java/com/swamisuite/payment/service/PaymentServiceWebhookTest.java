package com.swamisuite.payment.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.razorpay.RazorpayClient;
import com.swamisuite.payment.config.RazorpayProperties;
import com.swamisuite.payment.domain.WebhookEvent;
import com.swamisuite.payment.repository.CheckoutIntentRepository;
import com.swamisuite.payment.repository.RefundRepository;
import com.swamisuite.payment.repository.WebhookEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Proves two of the doc's non-negotiables (§7.2/§15.11): an invalid webhook
 * signature is rejected before touching any subscription/entitlement state, and a
 * duplicate webhook delivery (unique razorpay_event_id constraint violation) is a
 * no-op rather than being reprocessed.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceWebhookTest {

    @Mock
    private CheckoutIntentRepository checkoutIntentRepository;
    @Mock
    private WebhookEventRepository webhookEventRepository;
    @Mock
    private RefundRepository refundRepository;
    @Mock
    private SubscriptionServiceClient subscriptionServiceClient;
    @Mock
    private InvoiceService invoiceService;

    private PaymentService paymentService;
    private static final String SECRET = "test-webhook-secret";

    @BeforeEach
    void setUp() {
        RazorpayProperties properties = new RazorpayProperties("key", "secret", SECRET);
        WebhookSignatureVerifier verifier = new WebhookSignatureVerifier(properties);
        RazorpayClient razorpayClient = mock(RazorpayClient.class);
        paymentService = new PaymentService(checkoutIntentRepository, webhookEventRepository, refundRepository,
                subscriptionServiceClient, verifier, invoiceService, razorpayClient, properties);
    }

    @Test
    void handleWebhook_rejectsAnInvalidSignatureWithoutTouchingAnyState() {
        assertThatThrownBy(() -> paymentService.handleWebhook("{\"event\":\"payment.captured\"}", "not-a-real-signature", "evt-1"))
                .isInstanceOf(PaymentService.PaymentException.class);

        verify(webhookEventRepository, never()).save(any());
        verify(subscriptionServiceClient, never()).activate(any());
    }

    @Test
    void handleWebhook_duplicateDeliveryIsANoOp() throws Exception {
        String payload = "{\"event\":\"unrecognised.event\"}";
        String signature = computeSignature(payload);

        when(webhookEventRepository.save(any(WebhookEvent.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        // Must not throw - a duplicate delivery is swallowed as a no-op, not an error.
        paymentService.handleWebhook(payload, signature, "evt-1");

        verify(subscriptionServiceClient, never()).activate(any());
    }

    private String computeSignature(String payload) throws Exception {
        var mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new javax.crypto.spec.SecretKeySpec(SECRET.getBytes(java.nio.charset.StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(payload.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : raw) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
