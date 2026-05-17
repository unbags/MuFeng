package com.unbags.ordering.dto;

public class ChatAction {

    private String type;
    private Long dishId;
    private Integer quantity;
    private String operationId;

    public ChatAction() {
    }

    public ChatAction(String type, Long dishId, Integer quantity, String operationId) {
        this.type = type;
        this.dishId = dishId;
        this.quantity = quantity;
        this.operationId = operationId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDishId() {
        return dishId;
    }

    public void setDishId(Long dishId) {
        this.dishId = dishId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }
}
