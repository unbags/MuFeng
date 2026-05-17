package com.unbags.ordering.dto;

import java.math.BigDecimal;

public class ProductSalesItem {

    private Long dishId;
    private String dishName;
    private Integer quantity;
    private BigDecimal revenue;

    public ProductSalesItem() {
    }

    public ProductSalesItem(Long dishId, String dishName, Integer quantity, BigDecimal revenue) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.quantity = quantity;
        this.revenue = revenue;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }
}
