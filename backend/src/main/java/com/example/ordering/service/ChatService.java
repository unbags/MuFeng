package com.example.ordering.service;

import com.example.ordering.dto.ChatRequest;
import com.example.ordering.dto.ChatResponse;
import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.dto.OrderDetailResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private final MenuService menuService;
    private final OrderService orderService;

    public ChatService(MenuService menuService, OrderService orderService) {
        this.menuService = menuService;
        this.orderService = orderService;
    }

    public ChatResponse reply(ChatRequest request) {
        String orderNo = trimToNull(request.getOrderNo());
        if (orderNo != null) {
            OrderDetailResponse order = orderService.getOrder(orderNo);
            return new ChatResponse(
                "订单 " + order.getOrderNo() + " 当前状态为 " + order.getStatus() + statusHint(order.getStatus()),
                "ORDER",
                LocalDateTime.now()
            );
        }

        String message = trimToNull(request.getMessage());
        if (message != null && (message.contains("订单") || message.toLowerCase().contains("order"))) {
            return new ChatResponse("请提供订单号，我可以帮您查询当前制作或取餐状态。", "GUIDE", LocalDateTime.now());
        }

        MenuResponse menu = menuService.getMenu();
        List<DishResponse> dishes = menu.getDishes() == null ? java.util.Collections.emptyList() : menu.getDishes();
        String recommendations = dishes.stream()
            .limit(3)
            .map(dish -> dish.getName() + (trimToNull(dish.getHighlight()) == null ? "" : "（" + dish.getHighlight() + "）"))
            .collect(Collectors.joining("、"));

        if (recommendations.isEmpty()) {
            return new ChatResponse("当前菜单正在维护中，您可以稍后再试或咨询店员。", "MENU", LocalDateTime.now());
        }

        return new ChatResponse("今日推荐：" + recommendations + "。下单前请以页面展示价格和库存为准。", "MENU", LocalDateTime.now());
    }

    private String statusHint(String status) {
        if ("PENDING".equals(status)) {
            return "，店员会尽快接单。";
        }
        if ("CONFIRMED".equals(status)) {
            return "，订单已确认。";
        }
        if ("PREPARING".equals(status)) {
            return "，后厨正在制作。";
        }
        if ("READY".equals(status)) {
            return "，可以准备取餐。";
        }
        if ("DELIVERED".equals(status)) {
            return "，订单已完成。";
        }
        if ("CANCELLED".equals(status)) {
            return "，如有疑问请联系店员。";
        }
        return "。";
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
