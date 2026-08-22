package com.hotel.oms.socket;

public record AdminNotification(
    Long orderId,
    String tableNumber,
    String status,
    String message
) {}
