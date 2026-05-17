package com.unbags.ordering.ai.tools;

public class OrderStatusToolResponse {

    private String orderNo;
    private String status;
    private String hint;

    public OrderStatusToolResponse() {
    }

    public OrderStatusToolResponse(String orderNo, String status, String hint) {
        this.orderNo = orderNo;
        this.status = status;
        this.hint = hint;
    }

    public String orderNo() {
        return orderNo;
    }

    public String status() {
        return status;
    }

    public String hint() {
        return hint;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }
}
