package com.example.ordering.ai.tools;

import java.math.BigDecimal;

public record BusinessRuleToolResponse(BigDecimal serviceFee, String pickupRule) {
    // serviceFee: 堂食/外带服务费, 外带无额外费用, 始终为 BigDecimal.ZERO
}
