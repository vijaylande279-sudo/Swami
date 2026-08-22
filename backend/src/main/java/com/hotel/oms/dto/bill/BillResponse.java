package com.hotel.oms.dto.bill;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public record BillResponse(
    Long id,
    Long orderId,
    String tableNumber,
    String sessionType,
    String waiterName,
    List<BillItemResponse> items,
    BigDecimal subtotal,
    BigDecimal taxPercent,
    BigDecimal taxAmount,
    BigDecimal total,
    String paymentMode,
    String paymentUrl,
    String qrCodeBase64,
    boolean paid,
    OffsetDateTime generatedAt,
    OffsetDateTime paidAt
) {}
