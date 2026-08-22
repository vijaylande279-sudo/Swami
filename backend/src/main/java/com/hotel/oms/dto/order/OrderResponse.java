package com.hotel.oms.dto.order;

import java.time.OffsetDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String tableNumber,
    String status,
    String sessionType,
    String notes,
    List<OrderItemResponse> items,
    OffsetDateTime createdAt
) {}
