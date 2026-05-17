package com.unbags.ordering.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartSnapshot {

    private String cartId;
    private List<CartItemResponse> items = new ArrayList<>();
    private Integer itemCount = 0;
    private BigDecimal totalAmount = BigDecimal.ZERO;

    public CartSnapshot() {
    }

    public CartSnapshot(String cartId, List<CartItemResponse> items, Integer itemCount, BigDecimal totalAmount) {
        this.cartId = cartId;
        this.items = items;
        this.itemCount = itemCount;
        this.totalAmount = totalAmount;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public List<CartItemResponse> getItems() {
        return items;
    }

    public void setItems(List<CartItemResponse> items) {
        this.items = items;
    }

    public Integer getItemCount() {
        return itemCount;
    }

    public void setItemCount(Integer itemCount) {
        this.itemCount = itemCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }
}
