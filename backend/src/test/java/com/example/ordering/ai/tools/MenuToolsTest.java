package com.example.ordering.ai.tools;

import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.service.MenuService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MenuToolsTest {

    @Test
    void searchesDishesByKeywordAndBudget() {
        MenuService menuService = mock(MenuService.class);
        when(menuService.getMenu()).thenReturn(new MenuResponse(
            Collections.emptyList(),
            List.of(
                dish(1L, "南瓜鸡肉能量碗", "暖胃清淡", "signature", "32.00"),
                dish(2L, "香辣牛肉饭", "微辣下饭", "rice", "39.00")
            )
        ));

        MenuTools tools = new MenuTools(menuService);

        DishSearchResponse response = tools.searchDishes(new DishSearchRequest("南瓜", null, new BigDecimal("35.00")));

        assertThat(response.dishes()).hasSize(1);
        assertThat(response.dishes().get(0).name()).isEqualTo("南瓜鸡肉能量碗");
    }

    private DishResponse dish(Long id, String name, String highlight, String category, String price) {
        DishResponse dish = new DishResponse();
        dish.setId(id);
        dish.setName(name);
        dish.setHighlight(highlight);
        dish.setCategory(category);
        dish.setPrice(new BigDecimal(price));
        return dish;
    }
}
