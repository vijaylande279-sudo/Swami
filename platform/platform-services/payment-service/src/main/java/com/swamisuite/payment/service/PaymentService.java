package com.swamisuite.payment.service;

import com.razorpay.RazorpayClient;
import com.swamisuite.payment.domain.CheckoutIntent;
import com.swamisuite.payment.domain.CheckoutIntent.CheckoutIntentStatus;
import com.swamisuite.payment.domain.Invoice;
import com.swamisuite.payment.domain.Refund;
import com.swamisuite.payment.domain.WebhookEvent;
import com.swamisuite.payment.dto.PaymentDtos.CheckoutResponse;
import com.swamisuite.payment.dto.PaymentDtos.InvoiceResponse;
import com.swamisuite.payment.repository.CheckoutIntentRepository;
import com.swamisuite.payment.repository.RefundRepository;
import com.swamisuite.payment.repository.WebhookEventRepository;
import java.util.List;
import java.util.UUID;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final CheckoutIntentRepository checkoutIntentRepository;
    private final WebhookEventRepository webhookEventRepository;
    private final RefundRepository refundRepository;
    private final SubscriptionServiceClient subscriptionServiceClient;
    private final WebhookSignatureVerifier signatureVerifier;
    private final InvoiceService invoiceService;
    private final RazorpayClient razorpayClient;
    private final com.swamisuite.payment.config.RazorpayProperties razorpayProperties;

    public PaymentService(CheckoutIntentRepository checkoutIntentRepository, WebhookEventRepository webhookEventRepository,
                           RefundRepository refundRepository, SubscriptionServiceClient subscriptionServiceClient,
                           WebhookSignatureVerifier signatureVerifier, InvoiceService invoiceService,
                           RazorpayClient razorpayClient, com.swamisuite.payment.config.RazorpayProperties razorpayProperties) {
        this.checkoutIntentRepository = checkoutIntentRepository;
        this.webhookEventRepository = webhookEventRepository;
        this.refundRepository = refundRepository;
        this.subscriptionServiceClient = subscriptionServiceClient;
        this.signatureVerifier = signatureVerifier;
        this.invoiceService = invoiceService;
        this.razorpayClient = razorpayClient;
        this.razorpayProperties = razorpayProperties;
    }

    /**
     * The checkout body carries only an appKey - there is no amount field to send,
     * let alone trust. The price always comes from subscription-service's
     * server-computed checkout-intent (which itself re-fetches from catalog-service).
     */
    @Transactional
    public CheckoutResponse createCheckout(UUID tenantId, String appKey) {
        var intent = subscriptionServiceClient.createCheckoutIntent(tenantId, appKey);

        CheckoutIntent checkoutIntent = new CheckoutIntent(tenantId, intent.subscriptionId(), appKey,
                intent.amountPaise(), intent.gstPaise(), intent.totalPaise());

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", intent.totalPaise());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", checkoutIntent.getId() != null ? checkoutIntent.getId().toString() : UUID.randomUUID().toString());
            orderRequest.put("payment_capture", 1);
            var order = razorpayClient.orders.create(orderRequest);
            checkoutIntent.setRazorpayOrderId(order.get("id"));
        } catch (Exception e) {
            log.error("Razorpay order creation failed for tenant {} app {}: {}", tenantId, appKey, e.getMessage());
            throw new PaymentException("Could not start checkout - payment gateway unavailable");
        }

        checkoutIntent = checkoutIntentRepository.save(checkoutIntent);
        return new CheckoutResponse(checkoutIntent.getId(), checkoutIntent.getRazorpayOrderId(),
                checkoutIntent.getTotalPaise(), razorpayProperties.keyId());
    }

    /**
     * The webhook - never the browser redirect - is the only thing that grants
     * access, per doc §7.2/§15.1. Idempotent: a duplicate razorpay_event_id hits the
     * unique constraint and is treated as an already-processed no-op, since Razorpay
     * retries and every webhook must be assumed to arrive at least twice.
     */
    @Transactional
    public void handleWebhook(String rawPayload, String signature, String razorpayEventId) {
        if (!signatureVerifier.isValid(rawPayload, signature)) {
            log.warn("Rejected webhook with invalid signature (event id {})", razorpayEventId);
            throw new PaymentException("Invalid webhook signature");
        }

        try {
            webhookEventRepository.save(new WebhookEvent(razorpayEventId, rawPayload));
        } catch (DataIntegrityViolationException duplicate) {
            log.info("Duplicate webhook delivery for event {} - no-op", razorpayEventId);
            return;
        }

        JSONObject payload = new JSONObject(rawPayload);
        String event = payload.optString("event", "");

        if ("payment.captured".equals(event) || "order.paid".equals(event)) {
            String orderId = extractOrderId(payload);
            String paymentId = extractPaymentId(payload);
            checkoutIntentRepository.findByRazorpayOrderId(orderId).ifPresentOrElse(
                    ci -> activate(ci, paymentId),
                    () -> log.warn("Webhook for unknown Razorpay order {}", orderId));
        } else {
            log.info("Ignoring webhook event type: {}", event);
        }
    }

    private void activate(CheckoutIntent checkoutIntent, String razorpayPaymentId) {
        checkoutIntent.setStatus(CheckoutIntentStatus.PAID);
        checkoutIntent.setRazorpayPaymentId(razorpayPaymentId);
        checkoutIntentRepository.save(checkoutIntent);

        subscriptionServiceClient.activate(checkoutIntent.getSubscriptionId());

        invoiceService.generate(checkoutIntent.getTenantId(), checkoutIntent.getSubscriptionId(), checkoutIntent.getAppKey(),
                checkoutIntent.getAmountPaise(), checkoutIntent.getGstPaise(), checkoutIntent.getTotalPaise());
    }

    private String extractOrderId(JSONObject payload) {
        return extractPaymentEntityField(payload, "order_id");
    }

    private String extractPaymentId(JSONObject payload) {
        return extractPaymentEntityField(payload, "id");
    }

    private String extractPaymentEntityField(JSONObject payload, String field) {
        try {
            return payload.getJSONObject("payload").getJSONObject("payment").getJSONObject("entity").getString(field);
        } catch (Exception e) {
            return null;
        }
    }

    /** The browser-redirect landing page polls this - it only ever reads state the webhook already wrote, never mutates. */
    public String statusOf(UUID checkoutIntentId) {
        return checkoutIntentRepository.findById(checkoutIntentId)
                .map(ci -> ci.getStatus().name())
                .orElseThrow(() -> new PaymentException("Checkout intent not found"));
    }

    public List<InvoiceResponse> listInvoices(UUID tenantId) {
        return invoiceService.listForTenant(tenantId).stream()
                .map(i -> new InvoiceResponse(i.getId(), i.getInvoiceNumber(), i.getAmountPaise(), i.getGstPaise(), i.getTotalPaise(), i.getIssuedAt()))
                .toList();
    }

    /** Manual only, per doc §15.4/§15.10 - never automatic. */
    @Transactional
    public void refund(UUID checkoutIntentId, String reason, UUID initiatedBy) {
        CheckoutIntent checkoutIntent = checkoutIntentRepository.findById(checkoutIntentId)
                .orElseThrow(() -> new PaymentException("Checkout intent not found"));
        if (checkoutIntent.getRazorpayPaymentId() == null) {
            throw new PaymentException("Cannot refund a checkout intent with no captured payment");
        }

        Refund refund = new Refund(checkoutIntentId, checkoutIntent.getTotalPaise(), reason, initiatedBy);
        try {
            var razorpayRefund = razorpayClient.payments.refund(checkoutIntent.getRazorpayPaymentId(),
                    new JSONObject().put("amount", checkoutIntent.getTotalPaise()));
            refund.setRazorpayRefundId(razorpayRefund.get("id"));
            refund.setStatus(Refund.RefundStatus.PROCESSED);
        } catch (Exception e) {
            refund.setStatus(Refund.RefundStatus.FAILED);
            log.error("Refund failed for checkout intent {}: {}", checkoutIntentId, e.getMessage());
        }
        refundRepository.save(refund);
    }

    public static class PaymentException extends RuntimeException {
        public PaymentException(String message) {
            super(message);
        }
    }
}
