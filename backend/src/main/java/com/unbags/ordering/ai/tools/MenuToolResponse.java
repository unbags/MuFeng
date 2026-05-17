package com.unbags.ordering.ai.tools;

import java.util.List;

public class MenuToolResponse {

    private List<DishToolItem> dishes;

    public MenuToolResponse() {
    }

    public MenuToolResponse(List<DishToolItem> dishes) {
        this.dishes = dishes;
    }

    public List<DishToolItem> dishes() {
        return dishes;
    }

    public List<DishToolItem> getDishes() {
        return dishes;
    }

    public void setDishes(List<DishToolItem> dishes) {
        this.dishes = dishes;
    }
}
