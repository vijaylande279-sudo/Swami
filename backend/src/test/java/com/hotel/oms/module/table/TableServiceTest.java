package com.hotel.oms.module.table;

import com.hotel.oms.dto.table.TableResponse;
import com.hotel.oms.dto.table.UpdateTableStatusRequest;
import com.hotel.oms.exception.AppException;
import com.hotel.oms.module.order.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableServiceTest {

    @Mock
    private TableRepository tableRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private TableService tableService;

    private DiningTable table;

    @BeforeEach
    void setUp() {
        table = new DiningTable();
        table.setId(1L);
        table.setTableNumber("T1");
        table.setCapacity(4);
        table.setStatus(TableStatus.AVAILABLE);

        lenient().when(orderRepository.findActiveByTableIdIn(any())).thenReturn(List.of());
    }

    @Test
    void findAll_mapsEntitiesToResponses() {
        when(tableRepository.findAll()).thenReturn(List.of(table));

        List<TableResponse> result = tableService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).tableNumber()).isEqualTo("T1");
    }

    @Test
    void updateStatus_savesAndBroadcastsToTopic() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));
        when(tableRepository.save(any(DiningTable.class))).thenAnswer(inv -> inv.getArgument(0));

        TableResponse response = tableService.updateStatus(1L, new UpdateTableStatusRequest("OCCUPIED"));

        assertThat(response.status()).isEqualTo("OCCUPIED");
        verify(messagingTemplate).convertAndSend(eq("/topic/tables"), any(com.hotel.oms.socket.TableStatusEvent.class));
    }

    @Test
    void updateStatus_throwsForUnknownTable() {
        when(tableRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tableService.updateStatus(99L, new UpdateTableStatusRequest("OCCUPIED")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateStatus_rejectsInvalidStatusValue() {
        when(tableRepository.findById(1L)).thenReturn(Optional.of(table));

        assertThatThrownBy(() -> tableService.updateStatus(1L, new UpdateTableStatusRequest("UNKNOWN")))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("Invalid table status");
    }
}
