package com.hotel.oms.dto.order;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    Long menuItemId,
    String menuItemName,
    int quantity,
    BigDecimal unitPrice,
    String notes,
    String status
) {}
