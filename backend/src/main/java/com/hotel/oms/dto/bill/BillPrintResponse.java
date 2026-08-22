package com.hotel.oms.dto.bill;

import com.hotel.oms.dto.order.OrderItemResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BillPrintResponse(
    Long billId,
    Long orderId,
    String tableNumber,
    List<OrderItemResponse> items,
    BigDecimal subtotal,
    BigDecimal taxPercent,
    BigDecimal taxAmount,
    BigDecimal total,
    String paymentMode,
    String paymentUrl,
    String qrCodeBase64,
    boolean paid,
    OffsetDateTime generatedAt
) {}
