package com.unbags.ordering.service;

import com.unbags.ordering.dto.CartSnapshot;
import com.unbags.ordering.dto.DishResponse;
import com.unbags.ordering.dto.MenuResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CartServiceTest {

    private CartService cartService;

    @BeforeEach
    void setUp() {
        MenuService menuService = mock(MenuService.class);
        when(menuService.getMenu()).thenReturn(new MenuResponse(List.of(), List.of(
            dish(1L, "招牌牛肉饭", "28.00"),
            dish(2L, "柠檬茶", "12.00")
        )));
        cartService = new CartService(menuService);
    }

    @Test
    void addToCartMergesQuantityAndReturnsSnapshotTotal() {
        cartService.addToCart("cart-1", 1L, 2, "少盐", "op-1");

        CartSnapshot snapshot = cartService.addToCart("cart-1", 1L, 1, null, "op-2");

        assertThat(snapshot.getItems()).hasSize(1);
        assertThat(snapshot.getItems().get(0).getDishId()).isEqualTo(1L);
        assertThat(snapshot.getItems().get(0).getQuantity()).isEqualTo(3);
        assertThat(snapshot.getTotalAmount()).isEqualByComparingTo("84.00");
    }

    @Test
    void snapshotIncludesDishDescriptionAndHighlightForCartDisplay() {
        CartSnapshot snapshot = cartService.addToCart("cart-1", 1L, 1, null, "op-1");

        assertThat(snapshot.getItems().get(0).getDescription()).isEqualTo("米饭搭配牛肉");
        assertThat(snapshot.getItems().get(0).getHighlight()).isEqualTo("酱香浓郁");
    }

    @Test
    void addToCartIsIdempotentForSameOperationId() {
        cartService.addToCart("cart-1", 1L, 2, null, "op-1");

        CartSnapshot snapshot = cartService.addToCart("cart-1", 1L, 2, null, "op-1");

        assertThat(snapshot.getItems()).hasSize(1);
        assertThat(snapshot.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void rejectsInvalidQuantitiesBeforeMutatingCart() {
        assertThatThrownBy(() -> cartService.addToCart("cart-1", 1L, 0, null, "op-1"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("数量");

        assertThat(cartService.getCart("cart-1").getItems()).isEmpty();
    }

    @Test
    void clearCartRemovesBackendSnapshotItems() {
        cartService.addToCart("cart-1", 1L, 2, null, "op-1");
        cartService.addToCart("cart-1", 2L, 1, null, "op-2");

        CartSnapshot snapshot = cartService.clearCart("cart-1");

        assertThat(snapshot.getItems()).isEmpty();
        assertThat(snapshot.getItemCount()).isZero();
        assertThat(cartService.getCart("cart-1").getItems()).isEmpty();
    }

    private DishResponse dish(Long id, String name, String price) {
        DishResponse dish = new DishResponse();
        dish.setId(id);
        dish.setName(name);
        dish.setCategory("主食");
        dish.setPrice(new BigDecimal(price));
        dish.setDescription("米饭搭配牛肉");
        dish.setHighlight("酱香浓郁");
        return dish;
    }
}
