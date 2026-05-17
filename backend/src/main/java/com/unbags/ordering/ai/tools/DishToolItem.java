package com.unbags.ordering.ai.tools;

import java.math.BigDecimal;

public class DishToolItem {

    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String highlight;

    public DishToolItem() {
    }

    public DishToolItem(Long id, String name, String category, BigDecimal price, String highlight) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.highlight = highlight;
    }

    public Long id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String category() {
        return category;
    }

    public BigDecimal price() {
        return price;
    }

    public String highlight() {
        return highlight;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }
}
