package com.hotel.oms.socket;

public record TableStatusEvent(
    Long tableId,
    String tableNumber,
    String status,
    String borderColor
) {}
