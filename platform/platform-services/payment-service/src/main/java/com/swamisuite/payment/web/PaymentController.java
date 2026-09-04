package com.swamisuite.payment.web;

import com.swamisuite.common.security.JwtClaims;
import com.swamisuite.payment.dto.PaymentDtos.*;
import com.swamisuite.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Body carries only an appKey - there's no amount field to send, let alone trust. Doc §15.7: TENANT_ADMIN only. */
    @PostMapping("/checkout")
    @PreAuthorize("hasAuthority('platform:billing:purchase')")
    public CheckoutResponse checkout(Authentication authentication, @Valid @RequestBody CheckoutRequest request) {
        return paymentService.createCheckout(currentTenantId(authentication), request.appKey());
    }

    /** The browser-redirect landing page polls this - it only ever reads state the webhook already wrote. */
    @GetMapping("/status/{checkoutIntentId}")
    public CheckoutStatusResponse status(@PathVariable UUID checkoutIntentId) {
        return new CheckoutStatusResponse(paymentService.statusOf(checkoutIntentId));
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> invoices(Authentication authentication) {
        return paymentService.listInvoices(currentTenantId(authentication));
    }

    @PostMapping("/refunds/{checkoutIntentId}")
    @PreAuthorize("hasAuthority('platform:subscription:refund')")
    public void refund(Authentication authentication, @PathVariable UUID checkoutIntentId, @Valid @RequestBody RefundRequest request) {
        UUID initiatedBy = UUID.fromString(((JwtClaims) authentication.getPrincipal()).subject());
        paymentService.refund(checkoutIntentId, request.reason(), initiatedBy);
    }

    private UUID currentTenantId(Authentication authentication) {
        String tenantId = ((JwtClaims) authentication.getPrincipal()).tenantId();
        if (tenantId == null) {
            throw new PaymentService.PaymentException("No tenant context");
        }
        return UUID.fromString(tenantId);
    }
}
