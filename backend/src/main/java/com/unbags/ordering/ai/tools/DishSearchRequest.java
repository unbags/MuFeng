package com.unbags.ordering.ai.tools;

import java.math.BigDecimal;

public class DishSearchRequest {

    private String keyword;
    private String category;
    private BigDecimal maxPrice;

    public DishSearchRequest() {
    }

    public DishSearchRequest(String keyword, String category, BigDecimal maxPrice) {
        this.keyword = keyword;
        this.category = category;
        this.maxPrice = maxPrice;
    }

    public String keyword() {
        return keyword;
    }

    public String category() {
        return category;
    }

    public BigDecimal maxPrice() {
        return maxPrice;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(BigDecimal maxPrice) {
        this.maxPrice = maxPrice;
    }
}
