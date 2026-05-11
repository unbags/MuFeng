package com.example.ordering.ai.tools;

import java.math.BigDecimal;

public record BusinessRuleToolResponse(BigDecimal packageFee, BigDecimal deliveryFee, String pickupRule) {
}
