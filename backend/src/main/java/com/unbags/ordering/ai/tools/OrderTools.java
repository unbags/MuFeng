package com.unbags.ordering.ai.tools;

import com.unbags.ordering.dto.OrderDetailResponse;
import com.unbags.ordering.service.OrderService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderTools {

    private final OrderService orderService;
    private final ToolAuditSupport auditSupport;

    public OrderTools(OrderService orderService) {
        this(orderService, ToolAuditSupport.disabled());
    }

    @Autowired
    public OrderTools(OrderService orderService, ToolAuditSupport auditSupport) {
        this.orderService = orderService;
        this.auditSupport = auditSupport;
    }

    /**
     * 根据订单号查询订单实时状态，供 AI 助手回答订单进度问题。
     */
    @Tool(description = "Look up the real-time status of an order by its order number. Call when the user provides an order number or asks about order progress. 根据订单号查询订单实时状态。")
    public OrderStatusToolResponse getOrderStatus(OrderStatusToolRequest request) {
        String orderNo = request == null || request.orderNo() == null ? "" : request.orderNo().trim();
        return auditSupport.record("getOrderStatus", "orderNo=" + orderNo, () -> {
            OrderDetailResponse order = orderService.getOrder(orderNo);
            return new OrderStatusToolResponse(order.getOrderNo(), order.getStatus(), statusHint(order.getStatus()));
        });
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
        if ("READY".equals(status)) {
            return "订单已备好，可以准备取餐。";
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
