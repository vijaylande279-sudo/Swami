package com.hotel.oms.module.order;

import com.hotel.oms.dto.order.AddOrderItemRequest;
import com.hotel.oms.dto.order.CreateOrderRequest;
import com.hotel.oms.dto.order.OrderResponse;
import com.hotel.oms.exception.AppException;
import com.hotel.oms.module.menu.MenuItem;
import com.hotel.oms.module.menu.MenuItemRepository;
import com.hotel.oms.module.table.DiningTable;
import com.hotel.oms.module.table.TableService;
import com.hotel.oms.module.user.User;
import com.hotel.oms.module.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private MenuItemRepository menuItemRepository;
    @Mock
    private TableService tableService;
    @Mock
    private UserService userService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private OrderService orderService;

    private DiningTable table;
    private User waiter;
    private Order order;

    @BeforeEach
    void setUp() {
        table = new DiningTable();
        table.setId(1L);
        table.setTableNumber("T1");
        table.setCapacity(4);

        waiter = new User();
        waiter.setId(9L);
        waiter.setEmail("waiter@hotel.com");

        order = new Order();
        order.setId(100L);
        order.setTable(table);
        order.setWaiter(waiter);
        order.setStatus(OrderStatus.OPEN);
    }

    @Test
    void create_savesOrderWithOpenStatus() {
        when(tableService.findByIdOrThrow(1L)).thenReturn(table);
        when(userService.findByEmailOrThrow("waiter@hotel.com")).thenReturn(waiter);
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderResponse response = orderService.create(
                new CreateOrderRequest(1L, "LUNCH", "no onions"), "waiter@hotel.com");

        assertThat(response.status()).isEqualTo("OPEN");
        assertThat(response.tableNumber()).isEqualTo("T1");
    }

    @Test
    void send_throwsWhenOrderHasNoItems() {
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.send(100L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("no new items");
    }

    @Test
    void send_transitionsOpenOrderToSentToKitchen() {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(5L);
        menuItem.setName("Paneer Tikka");
        menuItem.setPrice(BigDecimal.valueOf(250));
        menuItem.setAvailable(true);

        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(menuItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        orderService.addItem(100L, new AddOrderItemRequest(5L, 2, null));
        OrderResponse response = orderService.send(100L);

        assertThat(response.status()).isEqualTo("SENT_TO_KITCHEN");
        assertThat(response.items()).hasSize(1);
    }

    @Test
    void send_rejectsInvalidTransitionFromBilled() {
        order.setStatus(OrderStatus.BILLED);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.send(100L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void addItem_rejectsWhenOrderIsBilled() {
        order.setStatus(OrderStatus.BILLED);
        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.addItem(100L, new AddOrderItemRequest(5L, 1, null)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("billed or closed");
    }

    @Test
    void addItem_rejectsUnavailableMenuItem() {
        MenuItem menuItem = new MenuItem();
        menuItem.setId(5L);
        menuItem.setAvailable(false);

        when(orderRepository.findByIdWithItems(100L)).thenReturn(Optional.of(order));
        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(menuItem));

        assertThatThrownBy(() -> orderService.addItem(100L, new AddOrderItemRequest(5L, 1, null)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not available");
    }
}
