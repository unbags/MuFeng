package com.unbags.ordering.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class MockPaymentService implements PaymentService {

    /**
     * 模拟支付成功并生成支付流水号。
     */
    @Override
    public PaymentResult pay(String orderNo, BigDecimal amount, String method) {
        return new PaymentResult(true, "MOCK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), "支付成功");
    }

    /**
     * 模拟退款成功并生成退款流水号。
     */
    @Override
    public PaymentResult refund(String orderNo, BigDecimal amount) {
        return new PaymentResult(true, "REFUND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(), "退款成功");
    }
}
