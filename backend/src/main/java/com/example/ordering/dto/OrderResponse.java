package com.example.ordering.dto;

import java.math.BigDecimal;
import java.util.List;

public class OrderResponse {

    private String orderNo;
    private String type;
    private String note;
    private BigDecimal subtotal;
    private BigDecimal packageFee;
    private BigDecimal deliveryFee;
    private BigDecimal total;
    private List<OrderReceiptItemResponse> items;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getPackageFee() {
        return packageFee;
    }

    public void setPackageFee(BigDecimal packageFee) {
        this.packageFee = packageFee;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(BigDecimal deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderReceiptItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderReceiptItemResponse> items) {
        this.items = items;
    }
}
