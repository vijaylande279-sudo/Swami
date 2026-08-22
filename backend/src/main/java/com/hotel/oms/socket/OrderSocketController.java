package com.hotel.oms.socket;

import com.hotel.oms.dto.order.UpdateItemStatusRequest;
import com.hotel.oms.module.order.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class OrderSocketController {

    private final OrderService orderService;

    @MessageMapping("/orders/{orderId}/items/{itemId}/status")
    public void updateItemStatus(
            @DestinationVariable Long orderId,
            @DestinationVariable Long itemId,
            UpdateItemStatusRequest request) {
        orderService.updateItemStatus(orderId, itemId, request);
    }
}
