package com.hotel.oms.module.menu;

import com.hotel.oms.dto.menu.CategoryWithItemsResponse;
import com.hotel.oms.dto.menu.MenuItemRequest;
import com.hotel.oms.dto.menu.MenuItemResponse;
import com.hotel.oms.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuCategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public List<CategoryWithItemsResponse> findCategoriesWithItems() {
        List<MenuCategory> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        List<MenuItem> items = menuItemRepository.findAllWithCategory();

        Map<Long, List<MenuItem>> itemsByCategory = items.stream()
                .filter(i -> i.getCategory() != null)
                .collect(Collectors.groupingBy(i -> i.getCategory().getId()));

        return categories.stream()
                .map(c -> new CategoryWithItemsResponse(
                        c.getId(),
                        c.getName(),
                        c.getSortOrder(),
                        itemsByCategory.getOrDefault(c.getId(), List.of()).stream()
                                .map(this::toResponse)
                                .toList()
                ))
                .toList();
    }

    public List<MenuItemResponse> findAllItems() {
        return menuItemRepository.findAllWithCategory().stream()
                .sorted(Comparator.comparing(MenuItem::getId))
                .map(this::toResponse)
                .toList();
    }

    public MenuItemResponse createItem(MenuItemRequest request) {
        MenuItem item = new MenuItem();
        applyRequest(item, request);
        return toResponse(menuItemRepository.save(item));
    }

    public MenuItemResponse updateItem(Long id, MenuItemRequest request) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException("Menu item not found", 404));
        applyRequest(item, request);
        return toResponse(menuItemRepository.save(item));
    }

    public void deleteItem(Long id) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException("Menu item not found", 404));
        item.setAvailable(false);
        menuItemRepository.save(item);
    }

    public MenuItemResponse setAvailability(Long id, boolean available) {
        MenuItem item = menuItemRepository.findById(id)
                .orElseThrow(() -> new AppException("Menu item not found", 404));
        item.setAvailable(available);
        return toResponse(menuItemRepository.save(item));
    }

    private void applyRequest(MenuItem item, MenuItemRequest request) {
        item.setName(request.name());
        item.setDescription(request.description());
        item.setPrice(request.price());
        item.setImageUrl(request.imageUrl());
        if (request.available() != null) {
            item.setAvailable(request.available());
        }
        if (request.categoryId() != null) {
            MenuCategory category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new AppException("Menu category not found", 404));
            item.setCategory(category);
        } else {
            item.setCategory(null);
        }
    }

    private MenuItemResponse toResponse(MenuItem item) {
        return new MenuItemResponse(
                item.getId(),
                item.getCategory() != null ? item.getCategory().getId() : null,
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.isAvailable(),
                item.getImageUrl()
        );
    }
}
