package com.hotel.oms.module.bill;

import com.hotel.oms.dto.bill.BillPrintResponse;
import com.hotel.oms.dto.bill.BillResponse;
import com.hotel.oms.dto.bill.PayBillRequest;
import com.hotel.oms.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    @PostMapping("/generate/{orderId}")
    public ResponseEntity<ApiResponse<BillResponse>> generate(@PathVariable Long orderId) {
        return ResponseEntity.status(201).body(ApiResponse.ok(billService.generate(orderId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BillResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billService.findById(id)));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<BillResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(billService.findByOrderId(orderId)));
    }

    @PatchMapping("/{id}/pay")
    public ResponseEntity<ApiResponse<BillResponse>> pay(
            @PathVariable Long id,
            @Valid @RequestBody PayBillRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(billService.pay(id, request)));
    }

    @GetMapping("/{id}/print")
    public ResponseEntity<ApiResponse<BillPrintResponse>> print(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(billService.print(id)));
    }
}
