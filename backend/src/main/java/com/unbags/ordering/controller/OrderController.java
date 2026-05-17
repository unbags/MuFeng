package com.unbags.ordering.controller;

import com.unbags.ordering.config.RateLimit;
import com.unbags.ordering.dto.ApiResponse;
import com.unbags.ordering.dto.OrderDetailResponse;
import com.unbags.ordering.dto.OrderRequest;
import com.unbags.ordering.dto.OrderResponse;
import com.unbags.ordering.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * 创建顾客订单，并返回订单小票信息。
     */
    @PostMapping
    @RateLimit(maxRequests = 20, windowSeconds = 60)
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    /**
     * 根据订单号查询订单详情和实时状态。
     */
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getOrder(orderNo));
    }
}
