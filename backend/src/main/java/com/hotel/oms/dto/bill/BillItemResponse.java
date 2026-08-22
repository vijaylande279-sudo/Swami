package com.hotel.oms.dto.bill;

import java.math.BigDecimal;

public record BillItemResponse(
    Long menuItemId,
    String name,
    int quantity,
    BigDecimal unitPrice,
    BigDecimal lineTotal
) {}
