package com.example.ordering.service;

import com.example.ordering.dto.CategoryResponse;
import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.dto.OrderDetailResponse;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatServiceTest {

    @Test
    void answersMenuRecommendationFromCurrentMenu() {
        MenuService menuService = mock(MenuService.class);
        OrderService orderService = mock(OrderService.class);
        when(menuService.getMenu()).thenReturn(new MenuResponse(
            Collections.singletonList(new CategoryResponse("signature", "招牌推荐", 1)),
            Collections.singletonList(dish("南瓜鸡肉能量碗", "暖胃又有满足感"))
        ));

        ChatService chatService = new ChatService(menuService, orderService);

        ChatResponse response = chatService.reply(new ChatRequest("今日招牌菜推荐", null));

        assertThat(response.getMessage()).contains("南瓜鸡肉能量碗");
        assertThat(response.getSource()).isEqualTo("MENU");
    }

    @Test
    void answersOrderStatusWhenOrderNoIsProvided() {
        MenuService menuService = mock(MenuService.class);
        OrderService orderService = mock(OrderService.class);
        OrderDetailResponse order = new OrderDetailResponse();
        order.setOrderNo("ORD1001");
        order.setStatus("READY");
        when(orderService.getOrder("ORD1001")).thenReturn(order);

        ChatService chatService = new ChatService(menuService, orderService);

        ChatResponse response = chatService.reply(new ChatRequest("查询我的订单", "ORD1001"));

        assertThat(response.getMessage()).contains("ORD1001", "READY");
        assertThat(response.getSource()).isEqualTo("ORDER");
    }

    private DishResponse dish(String name, String highlight) {
        DishResponse dish = new DishResponse();
        dish.setId(1L);
        dish.setName(name);
        dish.setCategory("signature");
        dish.setPrice(new BigDecimal("32.00"));
        dish.setHighlight(highlight);
        return dish;
    }
}
