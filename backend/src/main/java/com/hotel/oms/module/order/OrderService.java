package com.hotel.oms.module.order;

import com.hotel.oms.dto.order.AddOrderItemRequest;
import com.hotel.oms.dto.order.CreateOrderRequest;
import com.hotel.oms.dto.order.OrderItemResponse;
import com.hotel.oms.dto.order.OrderResponse;
import com.hotel.oms.dto.order.UpdateItemStatusRequest;
import com.hotel.oms.exception.AppException;
import com.hotel.oms.module.menu.MenuItem;
import com.hotel.oms.module.menu.MenuItemRepository;
import com.hotel.oms.module.table.DiningTable;
import com.hotel.oms.module.table.TableService;
import com.hotel.oms.module.table.TableStatus;
import com.hotel.oms.module.user.User;
import com.hotel.oms.module.user.UserService;
import com.hotel.oms.socket.AdminNotification;
import com.hotel.oms.socket.ItemStatusEvent;
import com.hotel.oms.socket.OrderNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final TableService tableService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
            OrderStatus.OPEN, Set.of(OrderStatus.SENT_TO_KITCHEN),
            OrderStatus.SENT_TO_KITCHEN, Set.of(OrderStatus.PREPARING),
            OrderStatus.PREPARING, Set.of(OrderStatus.READY),
            OrderStatus.READY, Set.of(OrderStatus.SERVED),
            OrderStatus.SERVED, Set.of(OrderStatus.READY_FOR_BILL, OrderStatus.SENT_TO_KITCHEN),
            OrderStatus.READY_FOR_BILL, Set.of(OrderStatus.BILLED),
            OrderStatus.BILLED, Set.of(OrderStatus.CLOSED)
    );

    @Transactional
    public OrderResponse create(CreateOrderRequest request, String waiterEmail) {
        DiningTable table = tableService.findByIdOrThrow(request.tableId());
        User waiter = userService.findByEmailOrThrow(waiterEmail);

        Order order = new Order();
        order.setTable(table);
        order.setWaiter(waiter);
        order.setSessionType(parseSessionType(request.sessionType()));
        order.setNotes(request.notes());
        order.setStatus(OrderStatus.OPEN);

        Order saved = orderRepository.save(order);

        table.setStatus(TableStatus.OCCUPIED);
        tableService.broadcastStatus(table);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        return toResponse(findOrderWithItemsOrThrow(id));
    }

    @Transactional
    public OrderResponse addItem(Long orderId, AddOrderItemRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);
        assertMutable(order);

        MenuItem menuItem = menuItemRepository.findById(request.menuItemId())
                .orElseThrow(() -> new AppException("Menu item not found", 404));

        if (!menuItem.isAvailable()) {
            throw new AppException("Menu item is not available", 400);
        }

        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMenuItem(menuItem);
        item.setQuantity(request.quantity());
        item.setUnitPrice(menuItem.getPrice());
        item.setNotes(request.notes());
        item.setStatus(OrderItemStatus.PENDING);

        order.getItems().add(item);
        orderItemRepository.save(item);

        return toResponse(order);
    }

    @Transactional
    public void removeItem(Long orderId, Long itemId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        assertMutable(order);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AppException("Order item not found", 404));

        if (item.getStatus() != OrderItemStatus.PENDING) {
            throw new AppException("Cannot remove an item that is already being prepared", 400);
        }

        order.getItems().remove(item);
        orderItemRepository.delete(item);
    }

    @Transactional
    public OrderResponse send(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.SENT_TO_KITCHEN);

        boolean hasPendingItems = order.getItems().stream().anyMatch(i -> i.getStatus() == OrderItemStatus.PENDING);
        if (!hasPendingItems) {
            throw new AppException("Cannot send an order to the kitchen with no new items", 400);
        }

        order.setStatus(OrderStatus.SENT_TO_KITCHEN);
        Order saved = orderRepository.save(order);

        if (saved.getTable() != null) {
            DiningTable table = saved.getTable();
            table.setStatus(TableStatus.OCCUPIED);
            tableService.broadcastStatus(table);
        }

        notifyKitchen(saved);

        return toResponse(saved);
    }

    @Transactional
    public OrderItemResponse updateItemStatus(Long orderId, Long itemId, UpdateItemStatusRequest request) {
        Order order = findOrderWithItemsOrThrow(orderId);

        OrderItem item = order.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new AppException("Order item not found", 404));

        OrderItemStatus newStatus = parseItemStatus(request.status());
        item.setStatus(newStatus);
        orderItemRepository.save(item);

        OrderItemResponse response = toItemResponse(item);

        if (order.getWaiter() != null) {
            messagingTemplate.convertAndSend("/topic/waiter/" + order.getWaiter().getId(),
                    new ItemStatusEvent(order.getId(), item.getId(), item.getMenuItem().getName(), item.getStatus().name()));
        }

        return response;
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findKitchenOrders() {
        return orderRepository.findByStatusInWithItems(List.of(OrderStatus.SENT_TO_KITCHEN, OrderStatus.PREPARING)).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findReadyOrders() {
        return orderRepository.findByStatusWithItems(OrderStatus.READY).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findReadyForBillOrders() {
        return orderRepository.findByStatusWithItems(OrderStatus.READY_FOR_BILL).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public OrderResponse markPreparing(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.PREPARING);

        order.setStatus(OrderStatus.PREPARING);
        Order saved = orderRepository.save(order);

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse markReady(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.READY);

        order.setStatus(OrderStatus.READY);
        Order saved = orderRepository.save(order);

        String tableNumber = saved.getTable() != null ? saved.getTable().getTableNumber() : null;
        messagingTemplate.convertAndSend("/topic/waiter",
                orderNotification(saved, "Order is READY for table " + tableNumber));

        if (saved.getTable() != null) {
            DiningTable table = saved.getTable();
            table.setStatus(TableStatus.ORDER_READY);
            tableService.broadcastStatus(table);
        }

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse markServed(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.SERVED);

        order.setStatus(OrderStatus.SERVED);
        Order saved = orderRepository.save(order);

        if (saved.getTable() != null) {
            DiningTable table = saved.getTable();
            table.setStatus(TableStatus.OCCUPIED);
            tableService.broadcastStatus(table);
        }

        return toResponse(saved);
    }

    @Transactional
    public OrderResponse markReadyForBill(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.READY_FOR_BILL);

        order.setStatus(OrderStatus.READY_FOR_BILL);
        Order saved = orderRepository.save(order);

        String tableNumber = saved.getTable() != null ? saved.getTable().getTableNumber() : null;

        if (saved.getTable() != null) {
            DiningTable table = saved.getTable();
            table.setStatus(TableStatus.READY_FOR_BILL);
            tableService.broadcastStatus(table);
        }

        messagingTemplate.convertAndSend("/topic/admin",
                new AdminNotification(saved.getId(), tableNumber, saved.getStatus().name(),
                        "Table " + tableNumber + " is ready for billing"));

        return toResponse(saved);
    }

    private OrderNotification orderNotification(Order order, String message) {
        String tableNumber = order.getTable() != null ? order.getTable().getTableNumber() : null;
        var items = order.getItems().stream()
                .map(i -> new OrderNotification.OrderItemInfo(i.getMenuItem().getName(), i.getQuantity()))
                .toList();

        return new OrderNotification(
                order.getId(),
                tableNumber,
                order.getSessionType() != null ? order.getSessionType().name() : null,
                order.getStatus().name(),
                message,
                items,
                order.getNotes(),
                order.getCreatedAt() != null ? order.getCreatedAt().toString() : null
        );
    }

    @Transactional
    public OrderResponse close(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.CLOSED);

        order.setStatus(OrderStatus.CLOSED);
        order.setClosedAt(java.time.OffsetDateTime.now());
        Order saved = orderRepository.save(order);

        if (saved.getTable() != null) {
            DiningTable table = saved.getTable();
            table.setStatus(TableStatus.AVAILABLE);
            tableService.broadcastStatus(table);
        }

        return toResponse(saved);
    }

    @Transactional
    public Order markBilled(Long orderId) {
        Order order = findOrderWithItemsOrThrow(orderId);
        validateTransition(order.getStatus(), OrderStatus.BILLED);
        order.setStatus(OrderStatus.BILLED);
        return orderRepository.save(order);
    }

    void validateTransition(OrderStatus current, OrderStatus next) {
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(next)) {
            throw new AppException("Invalid status transition: " + current + " -> " + next, 400);
        }
    }

    private void assertMutable(Order order) {
        if (order.getStatus() == OrderStatus.BILLED || order.getStatus() == OrderStatus.CLOSED) {
            throw new AppException("Cannot modify items on an order that is billed or closed", 400);
        }
    }

    private void notifyKitchen(Order order) {
        String tableNumber = order.getTable() != null ? order.getTable().getTableNumber() : null;
        messagingTemplate.convertAndSend("/topic/kitchen",
                orderNotification(order, "New order received from table " + tableNumber));
    }

    Order findOrderWithItemsOrThrow(Long id) {
        return orderRepository.findByIdWithItems(id)
                .orElseThrow(() -> new AppException("Order not found", 404));
    }

    private SessionType parseSessionType(String sessionType) {
        try {
            return SessionType.valueOf(sessionType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid session type: " + sessionType, 400);
        }
    }

    private OrderItemStatus parseItemStatus(String status) {
        try {
            return OrderItemStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid item status: " + status, 400);
        }
    }

    OrderResponse toResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getTable() != null ? order.getTable().getTableNumber() : null,
                order.getStatus().name(),
                order.getSessionType() != null ? order.getSessionType().name() : null,
                order.getNotes(),
                order.getItems().stream().map(this::toItemResponse).toList(),
                order.getCreatedAt()
        );
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        return new OrderItemResponse(
                item.getId(),
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getNotes(),
                item.getStatus().name()
        );
    }
}
