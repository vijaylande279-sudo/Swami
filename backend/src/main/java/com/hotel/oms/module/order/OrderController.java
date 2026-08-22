package com.hotel.oms.module.order;

import com.hotel.oms.dto.order.AddOrderItemRequest;
import com.hotel.oms.dto.order.CreateOrderRequest;
import com.hotel.oms.dto.order.OrderItemResponse;
import com.hotel.oms.dto.order.OrderResponse;
import com.hotel.oms.dto.order.UpdateItemStatusRequest;
import com.hotel.oms.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.status(201).body(ApiResponse.ok(orderService.create(request, user.getUsername())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findById(id)));
    }

    @GetMapping("/kitchen")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> kitchenOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findKitchenOrders()));
    }

    @GetMapping("/ready")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> readyOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findReadyOrders()));
    }

    @GetMapping("/ready-for-bill")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> readyForBillOrders() {
        return ResponseEntity.ok(ApiResponse.ok(orderService.findReadyForBillOrders()));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<ApiResponse<OrderResponse>> addItem(
            @PathVariable Long id,
            @Valid @RequestBody AddOrderItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.addItem(id, request)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public ResponseEntity<Void> removeItem(@PathVariable Long id, @PathVariable Long itemId) {
        orderService.removeItem(id, itemId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/send")
    public ResponseEntity<ApiResponse<OrderResponse>> send(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.send(id)));
    }

    @PatchMapping("/{id}/items/{itemId}/status")
    public ResponseEntity<ApiResponse<OrderItemResponse>> updateItemStatus(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateItemStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.updateItemStatus(id, itemId, request)));
    }

    @PatchMapping("/{id}/preparing")
    public ResponseEntity<ApiResponse<OrderResponse>> preparing(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.markPreparing(id)));
    }

    @PatchMapping("/{id}/ready")
    public ResponseEntity<ApiResponse<OrderResponse>> ready(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.markReady(id)));
    }

    @PatchMapping("/{id}/served")
    public ResponseEntity<ApiResponse<OrderResponse>> served(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.markServed(id)));
    }

    @PatchMapping("/{id}/ready-for-bill")
    public ResponseEntity<ApiResponse<OrderResponse>> readyForBill(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.markReadyForBill(id)));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<OrderResponse>> close(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(orderService.close(id)));
    }
}
