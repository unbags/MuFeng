package com.example.ordering.ai.tools;

import com.example.ordering.dto.OrderDetailResponse;
import com.example.ordering.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

    private final OrderService orderService;

    public OrderTools(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 根据订单号查询订单实时状态，供 AI 助手回答订单进度问题。
     */
    @Tool(description = "根据订单号查询订单实时状态。用户询问订单进度、制作状态或取餐状态时必须使用此工具。")
    public OrderStatusToolResponse getOrderStatus(OrderStatusToolRequest request) {
        String orderNo = request.orderNo() == null ? "" : request.orderNo().trim();
        OrderDetailResponse order = orderService.getOrder(orderNo);
        return new OrderStatusToolResponse(order.getOrderNo(), order.getStatus(), statusHint(order.getStatus()));
    }

    /**
     * 将订单状态枚举转换为顾客可理解的中文提示。
     */
    private String statusHint(String status) {
        if ("PENDING".equals(status)) {
            return "订单已提交，店员会尽快接单。";
        }
        if ("PREPARING".equals(status)) {
            return "后厨正在制作，请稍等。";
        }
        if ("COMPLETED".equals(status)) {
            return "订单已完成。";
        }
        if ("CANCELLED".equals(status)) {
            return "订单已取消，如有疑问请联系店员。";
        }
        return "请以订单页面展示的实时状态为准。";
    }
}
