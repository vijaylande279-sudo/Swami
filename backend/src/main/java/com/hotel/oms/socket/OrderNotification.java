package com.hotel.oms.socket;

import java.util.List;

public record OrderNotification(
    Long orderId,
    String tableNumber,
    String sessionType,
    String status,
    String message,
    List<OrderItemInfo> items,
    String notes,
    String createdAt
) {
    public record OrderItemInfo(String menuItemName, int quantity) {}
}
