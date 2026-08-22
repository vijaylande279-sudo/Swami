package com.hotel.oms.module.menu;

import com.hotel.oms.dto.menu.MenuItemRequest;
import com.hotel.oms.dto.menu.MenuItemResponse;
import com.hotel.oms.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuCategoryRepository categoryRepository;
    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuService menuService;

    private MenuCategory category;

    @BeforeEach
    void setUp() {
        category = new MenuCategory();
        category.setId(1L);
        category.setName("Starters");
    }

    @Test
    void createItem_savesWithResolvedCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(menuItemRepository.save(any(MenuItem.class))).thenAnswer(inv -> inv.getArgument(0));

        MenuItemResponse response = menuService.createItem(
                new MenuItemRequest(1L, "Spring Rolls", "Crispy", BigDecimal.valueOf(199), true, null));

        assertThat(response.name()).isEqualTo("Spring Rolls");
        assertThat(response.categoryId()).isEqualTo(1L);
    }

    @Test
    void createItem_throwsForUnknownCategory() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> menuService.createItem(
                new MenuItemRequest(99L, "Spring Rolls", "Crispy", BigDecimal.valueOf(199), true, null)))
                .isInstanceOf(AppException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void deleteItem_softDeletesByMarkingUnavailable() {
        MenuItem item = new MenuItem();
        item.setId(5L);
        item.setAvailable(true);

        when(menuItemRepository.findById(5L)).thenReturn(Optional.of(item));

        menuService.deleteItem(5L);

        ArgumentCaptor<MenuItem> captor = ArgumentCaptor.forClass(MenuItem.class);
        verify(menuItemRepository).save(captor.capture());
        assertThat(captor.getValue().isAvailable()).isFalse();
    }

    @Test
    void findCategoriesWithItems_groupsItemsUnderTheirCategory() {
        MenuItem item = new MenuItem();
        item.setId(1L);
        item.setName("Spring Rolls");
        item.setPrice(BigDecimal.TEN);
        item.setCategory(category);

        when(categoryRepository.findAllByOrderBySortOrderAsc()).thenReturn(List.of(category));
        when(menuItemRepository.findAllWithCategory()).thenReturn(List.of(item));

        var result = menuService.findCategoriesWithItems();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).items()).hasSize(1);
        assertThat(result.get(0).items().get(0).name()).isEqualTo("Spring Rolls");
    }
}
