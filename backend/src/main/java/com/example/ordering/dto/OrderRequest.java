package com.example.ordering.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public class OrderRequest {

    @NotBlank(message = "订单类型不能为空")
    private String orderType;

    @Size(max = 60, message = "订单备注不能超过60个字符")
    private String note;

    @Size(max = 32, message = "桌号不能超过32个字符")
    private String tableNumber;

    @Size(max = 32, message = "取餐号不能超过32个字符")
    private String pickupNumber;

    @Size(max = 64, message = "联系人不能超过64个字符")
    private String contactName;

    @Size(max = 32, message = "联系电话不能超过32个字符")
    private String contactPhone;

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

    public String getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(String tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getPickupNumber() {
        return pickupNumber;
    }

    public void setPickupNumber(String pickupNumber) {
        this.pickupNumber = pickupNumber;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public List<OrderItemRequest> getItems() {
        return items;
    }

    public void setItems(List<OrderItemRequest> items) {
        this.items = items;
    }
}
