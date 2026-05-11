package com.example.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.domain.CustomerOrder;
import com.example.ordering.domain.Dish;
import com.example.ordering.domain.OrderItem;
import com.example.ordering.dto.OrderDetailItemResponse;
import com.example.ordering.dto.OrderDetailResponse;
import com.example.ordering.dto.OrderItemRequest;
import com.example.ordering.dto.OrderReceiptItemResponse;
import com.example.ordering.dto.OrderRequest;
import com.example.ordering.dto.OrderResponse;
import com.example.ordering.mapper.CustomerOrderMapper;
import com.example.ordering.mapper.DishMapper;
import com.example.ordering.mapper.OrderItemMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final BigDecimal packageFee;
    private final BigDecimal deliveryFee;

    private final DishMapper dishMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final OrderWriteGuard orderWriteGuard;
    private final NotificationService notificationService;

    public OrderService(
        DishMapper dishMapper,
        CustomerOrderMapper customerOrderMapper,
        OrderItemMapper orderItemMapper,
        SnowflakeIdGenerator idGenerator,
        OrderWriteGuard orderWriteGuard,
        NotificationService notificationService,
        @Value("${app.order.package-fee:2.00}") BigDecimal packageFee,
        @Value("${app.order.delivery-fee:4.00}") BigDecimal deliveryFee
    ) {
        this.dishMapper = dishMapper;
        this.customerOrderMapper = customerOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.idGenerator = idGenerator;
        this.orderWriteGuard = orderWriteGuard;
        this.notificationService = notificationService;
        this.packageFee = packageFee;
        this.deliveryFee = deliveryFee;
    }

    /**
     * 创建订单，进入写入限流保护后执行实际下单逻辑。
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (!orderWriteGuard.tryAcquire()) {
            throw new IllegalStateException("当前下单人数较多，请稍后重试");
        }

        try {
            return doCreateOrder(request);
        } finally {
            orderWriteGuard.release();
        }
    }

    /**
     * 执行下单流程，包括校验菜品、扣减库存、计算费用和写入订单明细。
     */
    private OrderResponse doCreateOrder(OrderRequest request) {
        if (!"dine_in".equals(request.getOrderType()) && !"delivery".equals(request.getOrderType())) {
            throw new IllegalArgumentException("订单类型只能为堂食或外带");
        }
        String tableNumber = trimToNull(request.getTableNumber());
        String pickupNumber = "delivery".equals(request.getOrderType()) ? buildPickupNumber() : null;

        List<Long> dishIds = request.getItems().stream()
            .map(OrderItemRequest::getDishId)
            .distinct()
            .collect(Collectors.toList());

        List<Dish> dishes = dishMapper.selectList(
            new LambdaQueryWrapper<Dish>()
                .in(Dish::getId, dishIds)
                .eq(Dish::getAvailable, Boolean.TRUE)
                .eq(Dish::getDeleted, 0)
        );
        Map<Long, Dish> dishMap = dishes.stream()
            .collect(Collectors.toMap(Dish::getId, item -> item, (left, right) -> left, HashMap::new));

        if (dishMap.size() != dishIds.size()) {
            throw new IllegalArgumentException("部分商品不存在或已下架");
        }

        Map<Long, Integer> quantityByDishId = request.getItems().stream()
            .collect(Collectors.toMap(
                OrderItemRequest::getDishId,
                OrderItemRequest::getQuantity,
                Integer::sum,
                HashMap::new
            ));

        for (Map.Entry<Long, Integer> entry : quantityByDishId.entrySet()) {
            Dish dish = dishMap.get(entry.getKey());
            if (isLimitedStock(dish) && dish.getStock() < entry.getValue()) {
                throw new IllegalArgumentException("商品 '" + dish.getName() + "' 库存不足");
            }
        }

        for (Map.Entry<Long, Integer> entry : quantityByDishId.entrySet()) {
            Dish dish = dishMap.get(entry.getKey());
            if (isLimitedStock(dish)) {
                int updated = dishMapper.decrementStockIfEnough(dish.getId(), entry.getValue());
                if (updated != 1) {
                    throw new IllegalArgumentException("商品 '" + dish.getName() + "' 库存不足");
                }
            }
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        int itemCount = 0;
        List<OrderReceiptItemResponse> receiptItems = new ArrayList<>();
        for (OrderItemRequest item : request.getItems()) {
            Dish dish = dishMap.get(item.getDishId());
            BigDecimal lineTotal = dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            subtotal = subtotal.add(lineTotal);
            itemCount += item.getQuantity();
            receiptItems.add(new OrderReceiptItemResponse(dish.getName(), item.getQuantity(), lineTotal));

        }

        BigDecimal orderPackageFee = itemCount > 0 ? this.packageFee : BigDecimal.ZERO;
        BigDecimal orderDeliveryFee = "delivery".equals(request.getOrderType()) ? this.deliveryFee : BigDecimal.ZERO;
        BigDecimal total = subtotal.add(orderPackageFee).add(orderDeliveryFee);

        LocalDateTime now = LocalDateTime.now();
        long orderId = idGenerator.nextId();

        CustomerOrder order = new CustomerOrder();
        order.setId(orderId);
        order.setOrderNo("ORD" + orderId);
        order.setOrderType(request.getOrderType());
        order.setNote(trimToNull(request.getNote()));
        order.setTableNumber(tableNumber);
        order.setPickupNumber(pickupNumber);
        order.setContactName(trimToNull(request.getContactName()));
        order.setContactPhone(trimToNull(request.getContactPhone()));
        order.setSubtotal(subtotal);
        order.setPackageFee(orderPackageFee);
        order.setDeliveryFee(orderDeliveryFee);
        order.setTotalAmount(total);
        order.setItemCount(itemCount);
        order.setStatus("PENDING");
        order.setPaymentStatus("UNPAID");
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        customerOrderMapper.insert(order);

        for (OrderItemRequest item : request.getItems()) {
            Dish dish = dishMap.get(item.getDishId());
            OrderItem orderItem = new OrderItem();
            orderItem.setId(idGenerator.nextId());
            orderItem.setOrderId(orderId);
            orderItem.setDishId(dish.getId());
            orderItem.setDishName(dish.getName());
            orderItem.setDishPrice(dish.getPrice());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setLineTotal(dish.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItemMapper.insert(orderItem);
        }

        notificationService.notifyNewOrder(getOrder(order.getOrderNo()));
        notificationService.notifyDashboardUpdate();

        OrderResponse response = new OrderResponse();
        response.setOrderNo(order.getOrderNo());
        response.setType(order.getOrderType());
        response.setNote(order.getNote());
        response.setTableNumber(order.getTableNumber());
        response.setPickupNumber(order.getPickupNumber());
        response.setContactName(order.getContactName());
        response.setContactPhone(order.getContactPhone());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setSubtotal(order.getSubtotal());
        response.setPackageFee(order.getPackageFee());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotal(order.getTotalAmount());
        response.setItems(receiptItems);
        return response;
    }

    /**
     * 根据订单号查询订单详情和订单明细。
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrder(String orderNo) {
        CustomerOrder order = customerOrderMapper.selectOne(
            new LambdaQueryWrapper<CustomerOrder>().eq(CustomerOrder::getOrderNo, orderNo).last("limit 1")
        );
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<OrderDetailItemResponse> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId)
        ).stream().map(item -> {
            OrderDetailItemResponse response = new OrderDetailItemResponse();
            response.setDishId(item.getDishId());
            response.setName(item.getDishName());
            response.setPrice(item.getDishPrice());
            response.setQuantity(item.getQuantity());
            response.setTotal(item.getLineTotal());
            return response;
        }).collect(Collectors.toList());

        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderNo(order.getOrderNo());
        response.setOrderType(order.getOrderType());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setNote(order.getNote());
        response.setTableNumber(order.getTableNumber());
        response.setPickupNumber(order.getPickupNumber());
        response.setContactName(order.getContactName());
        response.setContactPhone(order.getContactPhone());
        response.setCancelReason(order.getCancelReason());
        response.setItemCount(order.getItemCount());
        response.setSubtotal(order.getSubtotal());
        response.setPackageFee(order.getPackageFee());
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setAcceptedAt(order.getAcceptedAt());
        response.setPreparingAt(order.getPreparingAt());
        response.setReadyAt(order.getReadyAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setCancelledAt(order.getCancelledAt());
        response.setItems(items);
        return response;
    }

    /**
     * 生成外带订单的取餐号。
     */
    private String buildPickupNumber() {
        return "P" + System.currentTimeMillis() % 1000000;
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
     * 判断菜品是否启用了有限库存控制。
     */
    private boolean isLimitedStock(Dish dish) {
        return dish.getStock() != null && dish.getStock() >= 0;
    }
}
