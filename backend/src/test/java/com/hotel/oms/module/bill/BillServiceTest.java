package com.hotel.oms.module.bill;

import com.hotel.oms.dto.bill.BillResponse;
import com.hotel.oms.exception.AppException;
import com.hotel.oms.module.menu.MenuItem;
import com.hotel.oms.module.order.Order;
import com.hotel.oms.module.order.OrderItem;
import com.hotel.oms.module.order.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillServiceTest {

    @Mock
    private BillRepository billRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private QRCodeService qrCodeService;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private BillService billService;

    private Order order;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(billService, "defaultTaxPercent", BigDecimal.valueOf(5));
        ReflectionTestUtils.setField(billService, "hotelName", "Swami Hotel");
        ReflectionTestUtils.setField(billService, "upiId", "swamihotel@upi");
        lenient().when(qrCodeService.generateBase64(any(), org.mockito.ArgumentMatchers.anyInt()))
                .thenReturn("fake-qr-base64");

        MenuItem menuItem = new MenuItem();
        menuItem.setId(1L);
        menuItem.setName("Butter Naan");

        OrderItem item = new OrderItem();
        item.setId(1L);
        item.setMenuItem(menuItem);
        item.setQuantity(3);
        item.setUnitPrice(BigDecimal.valueOf(10.1));

        order = new Order();
        order.setId(50L);
        order.setItems(List.of(item));
    }

    @Test
    void generate_computesSubtotalTaxAndTotalPrecisely() {
        when(billRepository.findByOrderId(50L)).thenReturn(Optional.empty());
        when(orderService.markBilled(50L)).thenReturn(order);
        when(billRepository.save(any(Bill.class))).thenAnswer(inv -> inv.getArgument(0));

        BillResponse response = billService.generate(50L);

        assertThat(response.subtotal()).isEqualByComparingTo("30.30");
        assertThat(response.taxAmount()).isEqualByComparingTo("1.52");
        assertThat(response.total()).isEqualByComparingTo("31.82");
    }

    @Test
    void generate_rejectsDuplicateBillForSameOrder() {
        when(billRepository.findByOrderId(50L)).thenReturn(Optional.of(new Bill()));

        assertThatThrownBy(() -> billService.generate(50L))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already been generated");
    }

    @Test
    void pay_rejectsAlreadyPaidBill() {
        Bill bill = new Bill();
        bill.setId(1L);
        bill.setPaid(true);
        when(billRepository.findById(1L)).thenReturn(Optional.of(bill));

        assertThatThrownBy(() -> billService.pay(1L, new com.hotel.oms.dto.bill.PayBillRequest("CASH")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("already paid");
    }
}
