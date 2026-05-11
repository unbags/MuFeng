package com.example.ordering.ai.tools;

import java.math.BigDecimal;

public record DishToolItem(Long id, String name, String category, BigDecimal price, String highlight) {
}
