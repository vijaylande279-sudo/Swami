package com.hotel.oms.socket;

import java.util.List;

public record NewOrderEvent(
    Long orderId,
    String tableNumber,
    List<NewOrderItem> items
) {
    public record NewOrderItem(
        Long orderItemId,
        String menuItemName,
        int quantity,
        String notes
    ) {}
}
