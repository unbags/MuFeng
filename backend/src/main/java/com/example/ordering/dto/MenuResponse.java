package com.example.ordering.dto;

import java.util.List;

public class MenuResponse {

    private List<CategoryResponse> categories;
    private List<DishResponse> dishes;

    public MenuResponse() {
    }

    public MenuResponse(List<CategoryResponse> categories, List<DishResponse> dishes) {
        this.categories = categories;
        this.dishes = dishes;
    }

    public List<CategoryResponse> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryResponse> categories) {
        this.categories = categories;
    }

    public List<DishResponse> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishResponse> dishes) {
        this.dishes = dishes;
    }
}
