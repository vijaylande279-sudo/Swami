package com.hotel.oms.module.menu;

import com.hotel.oms.dto.menu.CategoryWithItemsResponse;
import com.hotel.oms.dto.menu.MenuItemRequest;
import com.hotel.oms.dto.menu.MenuItemResponse;
import com.hotel.oms.dto.menu.UpdateAvailabilityRequest;
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
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryWithItemsResponse>>> categories() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.findCategoriesWithItems()));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> items() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.findAllItems()));
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<MenuItemResponse>> createItem(@Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.status(201).body(ApiResponse.ok(menuService.createItem(request)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.updateItem(id, request)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        menuService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/items/{id}/availability")
    public ResponseEntity<ApiResponse<MenuItemResponse>> setAvailability(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(menuService.setAvailability(id, request.available())));
    }
}
