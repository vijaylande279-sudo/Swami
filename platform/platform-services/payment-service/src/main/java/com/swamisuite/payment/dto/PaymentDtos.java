package com.swamisuite.payment.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.UUID;

public final class PaymentDtos {

    private PaymentDtos() {
    }

    public record CheckoutRequest(@NotBlank String appKey) {
    }

    public record CheckoutResponse(UUID checkoutIntentId, String razorpayOrderId, long totalPaise, String razorpayKeyId) {
    }

    public record CheckoutStatusResponse(String status) {
    }

    public record InvoiceResponse(UUID id, String invoiceNumber, long amountPaise, long gstPaise, long totalPaise, Instant issuedAt) {
    }

    public record RefundRequest(@NotBlank String reason) {
    }
}
