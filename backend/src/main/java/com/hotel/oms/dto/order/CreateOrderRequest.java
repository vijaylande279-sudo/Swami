package com.hotel.oms.dto.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(
    @NotNull Long tableId,
    @NotBlank String sessionType,
    String notes
) {}
