package com.example.ordering.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

public class OrderRequest {

    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    @Size(max = 60, message = "订单备注不能超过60个字符")
    private String note;

    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemRequest> items;

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
