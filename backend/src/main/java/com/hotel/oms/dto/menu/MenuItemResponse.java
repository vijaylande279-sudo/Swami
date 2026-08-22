package com.hotel.oms.dto.menu;

import java.math.BigDecimal;

public record MenuItemResponse(
    Long id,
    Long categoryId,
    String name,
    String description,
    BigDecimal price,
    boolean available,
    String imageUrl
) {}
