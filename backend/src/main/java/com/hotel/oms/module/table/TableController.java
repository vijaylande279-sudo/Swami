package com.hotel.oms.module.table;

import com.hotel.oms.dto.table.CreateTableRequest;
import com.hotel.oms.dto.table.TableResponse;
import com.hotel.oms.dto.table.UpdateTableStatusRequest;
import com.hotel.oms.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tables")
@RequiredArgsConstructor
public class TableController {

    private final TableService tableService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TableResponse>>> findAll() {
        return ResponseEntity.ok(ApiResponse.ok(tableService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.findByIdResponse(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TableResponse>> create(@Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok(tableService.create(request)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TableResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        tableService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TableResponse>> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTableStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(tableService.updateStatus(id, request)));
    }
}
