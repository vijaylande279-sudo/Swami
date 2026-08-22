package com.hotel.oms.dto.table;

public record TableResponse(
    Long id,
    String tableNumber,
    int capacity,
    String status,
    Long currentOrderId
) {}
