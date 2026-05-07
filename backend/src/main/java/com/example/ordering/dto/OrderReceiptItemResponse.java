package com.example.ordering.dto;

import java.math.BigDecimal;

public class OrderReceiptItemResponse {

    private String name;
    private Integer quantity;
    private BigDecimal total;

    public OrderReceiptItemResponse() {
    }

    public OrderReceiptItemResponse(String name, Integer quantity, BigDecimal total) {
        this.name = name;
        this.quantity = quantity;
        this.total = total;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
