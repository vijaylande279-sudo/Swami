package com.hotel.oms.module.bill;

import com.hotel.oms.dto.bill.BillItemResponse;
import com.hotel.oms.dto.bill.BillPrintResponse;
import com.hotel.oms.dto.bill.BillResponse;
import com.hotel.oms.dto.bill.PayBillRequest;
import com.hotel.oms.dto.order.OrderItemResponse;
import com.hotel.oms.exception.AppException;
import com.hotel.oms.module.order.Order;
import com.hotel.oms.module.order.OrderItem;
import com.hotel.oms.module.order.OrderService;
import com.hotel.oms.socket.AdminNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final OrderService orderService;
    private final QRCodeService qrCodeService;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${hotel.tax.percent}")
    private BigDecimal defaultTaxPercent;

    @Value("${hotel.name}")
    private String hotelName;

    @Value("${hotel.upi.id}")
    private String upiId;

    @Transactional
    public BillResponse generate(Long orderId) {
        if (billRepository.findByOrderId(orderId).isPresent()) {
            throw new AppException("A bill has already been generated for this order", 400);
        }

        Order order = orderService.markBilled(orderId);

        BigDecimal subtotal = order.getItems().stream()
                .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal taxPercent = defaultTaxPercent;
        BigDecimal taxAmount = subtotal
                .multiply(taxPercent)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal total = subtotal.add(taxAmount);

        String tableNumber = order.getTable() != null ? order.getTable().getTableNumber() : null;
        String upiUrl = String.format(
                "upi://pay?pa=%s&pn=%s&am=%s&cu=INR&tn=%s",
                urlEncode(upiId), urlEncode(hotelName), total.toPlainString(), urlEncode("Table" + tableNumber));
        String qrBase64 = qrCodeService.generateBase64(upiUrl, 300);

        Bill bill = new Bill();
        bill.setOrder(order);
        bill.setSubtotal(subtotal);
        bill.setTaxPercent(taxPercent);
        bill.setTaxAmount(taxAmount);
        bill.setTotal(total);
        bill.setPaymentUrl(upiUrl);
        bill.setQrCodeBase64(qrBase64);

        BillResponse response = toResponse(billRepository.save(bill));

        messagingTemplate.convertAndSend("/topic/admin",
                new AdminNotification(order.getId(), tableNumber, "BILLED",
                        "Bill generated for table " + tableNumber));

        return response;
    }

    @Transactional(readOnly = true)
    public BillResponse findById(Long id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Transactional(readOnly = true)
    public BillResponse findByOrderId(Long orderId) {
        return toResponse(billRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException("No bill has been generated for this order yet", 404)));
    }

    @Transactional
    public BillResponse pay(Long id, PayBillRequest request) {
        Bill bill = findByIdOrThrow(id);

        if (bill.isPaid()) {
            throw new AppException("Bill is already paid", 400);
        }

        bill.setPaymentMode(parsePaymentMode(request.paymentMode()));
        bill.setPaid(true);
        bill.setPaidAt(OffsetDateTime.now());

        BillResponse response = toResponse(billRepository.save(bill));

        Order order = bill.getOrder();
        String tableNumber = order.getTable() != null ? order.getTable().getTableNumber() : null;
        orderService.close(order.getId());

        messagingTemplate.convertAndSend("/topic/admin",
                new AdminNotification(order.getId(), tableNumber, "PAID",
                        "Payment received for table " + tableNumber));

        return response;
    }

    @Transactional(readOnly = true)
    public BillPrintResponse print(Long id) {
        Bill bill = findByIdOrThrow(id);
        Order order = bill.getOrder();

        var items = order.getItems().stream().map(this::toItemResponse).toList();

        return new BillPrintResponse(
                bill.getId(),
                order.getId(),
                order.getTable() != null ? order.getTable().getTableNumber() : null,
                items,
                bill.getSubtotal(),
                bill.getTaxPercent(),
                bill.getTaxAmount(),
                bill.getTotal(),
                bill.getPaymentMode() != null ? bill.getPaymentMode().name() : null,
                bill.getPaymentUrl(),
                bill.getQrCodeBase64(),
                bill.isPaid(),
                bill.getGeneratedAt()
        );
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private Bill findByIdOrThrow(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new AppException("Bill not found", 404));
    }

    private PaymentMode parsePaymentMode(String mode) {
        try {
            return PaymentMode.valueOf(mode.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid payment mode: " + mode, 400);
        }
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

    private BillItemResponse toBillItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return new BillItemResponse(
                item.getMenuItem().getId(),
                item.getMenuItem().getName(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal
        );
    }

    private BillResponse toResponse(Bill bill) {
        Order order = bill.getOrder();
        var items = order.getItems().stream().map(this::toBillItemResponse).toList();

        return new BillResponse(
                bill.getId(),
                order.getId(),
                order.getTable() != null ? order.getTable().getTableNumber() : null,
                order.getSessionType() != null ? order.getSessionType().name() : null,
                order.getWaiter() != null ? order.getWaiter().getName() : null,
                items,
                bill.getSubtotal(),
                bill.getTaxPercent(),
                bill.getTaxAmount(),
                bill.getTotal(),
                bill.getPaymentMode() != null ? bill.getPaymentMode().name() : null,
                bill.getPaymentUrl(),
                bill.getQrCodeBase64(),
                bill.isPaid(),
                bill.getGeneratedAt(),
                bill.getPaidAt()
        );
    }
}
