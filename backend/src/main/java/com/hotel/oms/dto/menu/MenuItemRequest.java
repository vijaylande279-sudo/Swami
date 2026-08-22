package com.hotel.oms.dto.menu;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record MenuItemRequest(
    Long categoryId,
    @NotBlank String name,
    String description,
    @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
    Boolean available,
    String imageUrl
) {}
