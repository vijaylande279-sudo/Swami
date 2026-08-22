package com.hotel.oms.socket;

public record ItemStatusEvent(
    Long orderId,
    Long orderItemId,
    String menuItemName,
    String status
) {}
