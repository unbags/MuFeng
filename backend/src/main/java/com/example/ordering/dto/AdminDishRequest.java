package com.example.ordering.dto;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigDecimal;

public class AdminDishRequest {

    @NotBlank(message = "商品名称不能为空")
    @Size(max = 64, message = "商品名称不能超过64个字符")
    private String name;

    @NotBlank(message = "商品分类不能为空")
    @Size(max = 32, message = "分类编号不能超过32个字符")
    private String categoryId;

    @NotNull(message = "商品价格不能为空")
    @DecimalMin(value = "0.01", message = "商品价格必须大于0")
    private BigDecimal price;

    @NotNull(message = "商品评分不能为空")
    @DecimalMin(value = "0.0", message = "商品评分不能小于0")
    private BigDecimal rating;

    @NotNull(message = "商品热量不能为空")
    @Min(value = 0, message = "商品热量不能为负数")
    @Max(value = 9999, message = "商品热量不能超过9999")
    private Integer calories;

    @NotBlank(message = "商品介绍不能为空")
    @Size(max = 255, message = "商品介绍不能超过255个字符")
    private String description;

    @NotBlank(message = "商品文案不能为空")
    @Size(max = 128, message = "商品文案不能超过128个字符")
    private String highlight;

    @Size(max = 255, message = "图片地址不能超过255个字符")
    private String imageUrl;

    @NotNull(message = "上下架状态不能为空")
    private Boolean available;

    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Integer getCalories() {
        return calories;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }
}
