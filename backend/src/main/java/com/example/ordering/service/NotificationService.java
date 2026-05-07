package com.example.ordering.service;

import com.example.ordering.dto.OrderDetailResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void notifyNewOrder(OrderDetailResponse order) {
        messagingTemplate.convertAndSend("/topic/orders/new", order);
    }

    public void notifyOrderStatusChanged(OrderDetailResponse order) {
        messagingTemplate.convertAndSend("/topic/orders/status", order);
    }

    public void notifyDashboardUpdate() {
        messagingTemplate.convertAndSend("/topic/dashboard", "update");
    }
}
