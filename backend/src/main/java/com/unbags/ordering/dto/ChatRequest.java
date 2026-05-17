package com.unbags.ordering.dto;

import jakarta.validation.constraints.Size;

public class ChatRequest {

    @Size(max = 500, message = "问题不能超过500个字符")
    private String message;

    @Size(max = 32, message = "订单号不能超过32个字符")
    private String orderNo;

    @Size(max = 64, message = "会话ID不能超过64个字符")
    private String conversationId;

    @Size(max = 96, message = "购物车ID不能超过96个字符")
    private String cartId;

    public ChatRequest() {
    }

    public ChatRequest(String message, String orderNo) {
        this.message = message;
        this.orderNo = orderNo;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String orderNo) {
        this.orderNo = orderNo;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }
}
