package com.unbags.ordering.service;

import com.unbags.ordering.dto.OrderDetailResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 向后台推送新订单通知。
     */
    public void notifyNewOrder(OrderDetailResponse order) {
        messagingTemplate.convertAndSend("/topic/orders/new", order);
    }

    /**
     * 向后台推送订单状态变更通知。
     */
    public void notifyOrderStatusChanged(OrderDetailResponse order) {
        messagingTemplate.convertAndSend("/topic/orders/status", order);
    }

    /**
     * 通知后台仪表盘刷新统计数据。
     */
    public void notifyDashboardUpdate() {
        messagingTemplate.convertAndSend("/topic/dashboard", "update");
    }
}
