package com.unbags.ordering.ai.tools;

public class OrderStatusToolRequest {

    private String orderNo;

    public OrderStatusToolRequest() {
    }

    public OrderStatusToolRequest(String orderNo) {
        this.orderNo = orderNo;
    }

    public String orderNo() {
        return orderNo;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }
}
