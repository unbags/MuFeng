package com.example.ordering.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.example.ordering.domain.CustomerOrder;
import com.example.ordering.domain.Dish;
import com.example.ordering.domain.OrderItem;
import com.example.ordering.dto.OrderItemRequest;
import com.example.ordering.dto.OrderRequest;
import com.example.ordering.dto.OrderResponse;
import com.example.ordering.mapper.CustomerOrderMapper;
import com.example.ordering.mapper.DishMapper;
import com.example.ordering.mapper.OrderItemMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private DishMapper dishMapper;
    private CustomerOrderMapper customerOrderMapper;
    private OrderItemMapper orderItemMapper;
    private OrderWriteGuard orderWriteGuard;
    private NotificationService notificationService;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        dishMapper = mock(DishMapper.class);
        customerOrderMapper = mock(CustomerOrderMapper.class);
        orderItemMapper = mock(OrderItemMapper.class);
        SnowflakeIdGenerator idGenerator = mock(SnowflakeIdGenerator.class);
        orderWriteGuard = mock(OrderWriteGuard.class);
        notificationService = mock(NotificationService.class);

        when(orderWriteGuard.tryAcquire()).thenReturn(true);
        when(idGenerator.nextId()).thenReturn(1001L, 2001L);

        Dish dish = new Dish();
        dish.setId(1L);
        dish.setName("南瓜鸡肉能量碗");
        dish.setPrice(new BigDecimal("32.00"));
        dish.setAvailable(true);
        dish.setDeleted(0);
        dish.setStock(-1);
        when(dishMapper.selectList(ArgumentMatchers.<Wrapper<Dish>>any())).thenReturn(Collections.singletonList(dish));

        AtomicReference<CustomerOrder> savedOrder = new AtomicReference<>();
        when(customerOrderMapper.insert(any(CustomerOrder.class))).thenAnswer(invocation -> {
            savedOrder.set(invocation.getArgument(0));
            return 1;
        });
        when(customerOrderMapper.selectOne(ArgumentMatchers.<Wrapper<CustomerOrder>>any()))
            .thenAnswer(invocation -> savedOrder.get());
        when(orderItemMapper.selectList(ArgumentMatchers.<Wrapper<OrderItem>>any())).thenReturn(Collections.emptyList());

        orderService = new OrderService(
            dishMapper,
            customerOrderMapper,
            orderItemMapper,
            idGenerator,
            orderWriteGuard,
            notificationService,
            new BigDecimal("4.00")
        );
    }

    @Test
    void createDineInOrderPersistsTableAndContactMetadata() {
        OrderRequest request = baseRequest("dine_in");
        request.setTableNumber("A05");
        request.setContactName("张三");
        request.setContactPhone("13800138000");

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getTableNumber()).isEqualTo("A05");
        assertThat(response.getContactName()).isEqualTo("张三");
        assertThat(response.getContactPhone()).isEqualTo("13800138000");
        assertThat(response.getPaymentStatus()).isEqualTo("UNPAID");
        assertThat(response.getPickupNumber()).isNull();
    }

    @Test
    void createTakeoutOrderGeneratesServerPickupNumber() {
        OrderRequest request = baseRequest("delivery");
        request.setPickupNumber("CLIENT-123");

        OrderResponse response = orderService.createOrder(request);

        assertThat(response.getPickupNumber()).startsWith("P");
        assertThat(response.getPickupNumber()).isNotEqualTo("CLIENT-123");
        assertThat(response.getPaymentStatus()).isEqualTo("UNPAID");
    }

    private OrderRequest baseRequest(String orderType) {
        OrderItemRequest item = new OrderItemRequest();
        item.setDishId(1L);
        item.setQuantity(2);

        OrderRequest request = new OrderRequest();
        request.setOrderType(orderType);
        request.setItems(Collections.singletonList(item));
        return request;
    }
}
