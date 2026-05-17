package com.unbags.ordering.service;

import com.unbags.ordering.dto.ChatRequest;
import com.unbags.ordering.dto.ChatResponse;
import com.unbags.ordering.dto.DishResponse;
import com.unbags.ordering.dto.MenuResponse;
import com.unbags.ordering.dto.OrderDetailResponse;
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

    /**
     * 根据顾客消息或订单号生成规则兜底回复。
     */
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

        // 根据消息内容判断意图，提供多样化的回复
        if (message != null) {
            if (containsAny(message, "推荐", "好吃", "招牌", "吃什么")) {
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

            if (containsAny(message, "营业", "时间", "开门")) {
                return new ChatResponse("您好，午餐 11:00-14:00，晚餐 17:00-21:30。具体以门店公告为准。", "GUIDE", LocalDateTime.now());
            }

            if (containsAny(message, "支付", "付款", "微信", "支付宝")) {
                return new ChatResponse("我们支持微信支付和支付宝在线支付，也可以到店现金或刷卡支付。", "GUIDE", LocalDateTime.now());
            }

            if (containsAny(message, "外带", "取餐")) {
                return new ChatResponse("下单时选择外带，提交后系统会分配取餐号，一般等待15-20分钟。", "GUIDE", LocalDateTime.now());
            }

            if (containsAny(message, "退款", "取消")) {
                return new ChatResponse("如需退款，请联系店内店员或拨打门店电话处理。", "GUIDE", LocalDateTime.now());
            }
        }

        // 通用引导回复
        return new ChatResponse("您好！我是沐枫点餐助手。我可以帮您推荐菜品、查询订单状态、了解外带取餐和支付方式。请问有什么可以帮您的？", "GUIDE", LocalDateTime.now());
    }

    /**
     * 根据订单状态生成面向顾客的状态说明。
     */
    private String statusHint(String status) {
        if ("PENDING".equals(status)) {
            return "，店员会尽快接单。";
        }
        if ("PREPARING".equals(status)) {
            return "，后厨正在制作。";
        }
        if ("COMPLETED".equals(status)) {
            return "，订单已完成。";
        }
        if ("CANCELLED".equals(status)) {
            return "，如有疑问请联系店员。";
        }
        return "。";
    }

    /**
     * 去除字符串首尾空白，并将空字符串转换为 null。
     */
    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * 判断文本中是否包含任意一个关键词。
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
