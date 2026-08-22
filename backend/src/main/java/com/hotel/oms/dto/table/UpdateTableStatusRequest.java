package com.hotel.oms.dto.table;

import jakarta.validation.constraints.NotBlank;

public record UpdateTableStatusRequest(
    @NotBlank String status
) {}
