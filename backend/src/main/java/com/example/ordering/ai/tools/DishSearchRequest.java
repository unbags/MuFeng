package com.example.ordering.ai.tools;

import java.math.BigDecimal;

public record DishSearchRequest(String keyword, String category, BigDecimal maxPrice) {
}
