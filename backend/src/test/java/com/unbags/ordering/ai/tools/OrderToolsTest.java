package com.unbags.ordering.ai.tools;

import com.unbags.ordering.dto.OrderDetailResponse;
import com.unbags.ordering.service.OrderService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderToolsTest {

    @Test
    void returnsOrderStatusWithCustomerSafeHint() {
        OrderService orderService = mock(OrderService.class);
        OrderDetailResponse order = new OrderDetailResponse();
        order.setOrderNo("ORD1001");
        order.setStatus("READY");
        when(orderService.getOrder("ORD1001")).thenReturn(order);

        OrderTools tools = new OrderTools(orderService);

        OrderStatusToolResponse response = tools.getOrderStatus(new OrderStatusToolRequest("ORD1001"));

        assertThat(response.orderNo()).isEqualTo("ORD1001");
        assertThat(response.status()).isEqualTo("READY");
        assertThat(response.hint()).contains("取餐");
    }
}
