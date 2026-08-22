package com.hotel.oms.dto.bill;

import jakarta.validation.constraints.NotBlank;

public record PayBillRequest(
    @NotBlank String paymentMode
) {}
