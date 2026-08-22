package com.hotel.oms.dto.menu;

import java.util.List;

public record CategoryWithItemsResponse(
    Long id,
    String name,
    int sortOrder,
    List<MenuItemResponse> items
) {}
