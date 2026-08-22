package com.hotel.oms.dto.table;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateTableRequest(
    @NotBlank String tableNumber,
    @Min(1) int capacity
) {}
