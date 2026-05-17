package com.unbags.ordering.dto;

import java.math.BigDecimal;

public class CartItemResponse {

    private Long dishId;
    private String dishName;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal lineTotal;
    private String remark;
    private String imageUrl;
    private String description;
    private String highlight;

    public CartItemResponse() {
    }

    public CartItemResponse(Long dishId, String dishName, String category, BigDecimal price,
                            Integer quantity, BigDecimal lineTotal, String remark, String imageUrl) {
        this(dishId, dishName, category, price, quantity, lineTotal, remark, imageUrl, null, null);
    }

    public CartItemResponse(Long dishId, String dishName, String category, BigDecimal price,
                            Integer quantity, BigDecimal lineTotal, String remark, String imageUrl,
                            String description, String highlight) {
        this.dishId = dishId;
        this.dishName = dishName;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
        this.remark = remark;
        this.imageUrl = imageUrl;
        this.description = description;
        this.highlight = highlight;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }

    public void setLineTotal(BigDecimal lineTotal) {
        this.lineTotal = lineTotal;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
}
