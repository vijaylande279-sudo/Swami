package com.hotel.oms.module.table;

import com.hotel.oms.dto.table.CreateTableRequest;
import com.hotel.oms.dto.table.TableResponse;
import com.hotel.oms.dto.table.UpdateTableStatusRequest;
import com.hotel.oms.exception.AppException;
import com.hotel.oms.module.order.Order;
import com.hotel.oms.module.order.OrderRepository;
import com.hotel.oms.socket.TableStatusEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<TableResponse> findAll() {
        List<DiningTable> tables = tableRepository.findAll();
        List<Long> tableIds = tables.stream().map(DiningTable::getId).toList();

        Map<Long, Long> currentOrderByTableId = orderRepository.findActiveByTableIdIn(tableIds).stream()
                .collect(Collectors.toMap(o -> o.getTable().getId(), Order::getId, (a, b) -> a));

        return tables.stream()
                .map(t -> toResponse(t, currentOrderByTableId.get(t.getId())))
                .toList();
    }

    public TableResponse findByIdResponse(Long id) {
        return toResponse(findByIdOrThrow(id), currentOrderId(id));
    }

    public TableResponse create(CreateTableRequest request) {
        if (tableRepository.existsByTableNumber(request.tableNumber())) {
            throw new AppException("Table number already exists", 400);
        }

        DiningTable table = new DiningTable();
        table.setTableNumber(request.tableNumber());
        table.setCapacity(request.capacity());
        table.setStatus(TableStatus.AVAILABLE);

        return toResponse(tableRepository.save(table), null);
    }

    public TableResponse update(Long id, CreateTableRequest request) {
        DiningTable table = findByIdOrThrow(id);

        if (!table.getTableNumber().equals(request.tableNumber())
                && tableRepository.existsByTableNumber(request.tableNumber())) {
            throw new AppException("Table number already exists", 400);
        }

        table.setTableNumber(request.tableNumber());
        table.setCapacity(request.capacity());

        return toResponse(tableRepository.save(table), currentOrderId(id));
    }

    public void delete(Long id) {
        DiningTable table = findByIdOrThrow(id);

        if (orderRepository.existsByTableId(id)) {
            throw new AppException("Cannot delete a table that has existing orders", 400);
        }

        tableRepository.delete(table);
    }

    public TableResponse updateStatus(Long id, UpdateTableStatusRequest request) {
        DiningTable table = tableRepository.findById(id)
                .orElseThrow(() -> new AppException("Table not found", 404));

        TableStatus status = parseStatus(request.status());
        table.setStatus(status);
        DiningTable saved = tableRepository.save(table);

        TableResponse response = toResponse(saved, currentOrderId(id));
        broadcastStatus(saved);

        return response;
    }

    public DiningTable findByIdOrThrow(Long id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new AppException("Table not found", 404));
    }

    public void broadcastStatus(DiningTable table) {
        messagingTemplate.convertAndSend("/topic/tables",
                new TableStatusEvent(table.getId(), table.getTableNumber(), table.getStatus().name(),
                        table.getStatus().borderColor()));
    }

    private Long currentOrderId(Long tableId) {
        return orderRepository.findActiveByTableIdIn(List.of(tableId)).stream()
                .map(Order::getId)
                .findFirst()
                .orElse(null);
    }

    private TableStatus parseStatus(String status) {
        try {
            return TableStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid table status: " + status, 400);
        }
    }

    private TableResponse toResponse(DiningTable table, Long currentOrderId) {
        return new TableResponse(table.getId(), table.getTableNumber(), table.getCapacity(), table.getStatus().name(), currentOrderId);
    }
}
