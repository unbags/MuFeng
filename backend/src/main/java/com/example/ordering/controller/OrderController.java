package com.example.ordering.controller;

import com.example.ordering.config.RateLimit;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.OrderDetailResponse;
import com.example.ordering.dto.OrderRequest;
import com.example.ordering.dto.OrderResponse;
import com.example.ordering.service.OrderService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @RateLimit(maxRequests = 20, windowSeconds = 60)
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        return ApiResponse.success(orderService.createOrder(request));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailResponse> getOrder(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getOrder(orderNo));
    }
}
