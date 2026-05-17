package com.unbags.ordering.ai.tools;

import com.unbags.ordering.dto.CartSnapshot;
import com.unbags.ordering.service.CartService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CartTools {

    private final CartService cartService;
    private final ToolAuditSupport auditSupport;

    public CartTools(CartService cartService) {
        this(cartService, ToolAuditSupport.disabled());
    }

    @Autowired
    public CartTools(CartService cartService, ToolAuditSupport auditSupport) {
        this.cartService = cartService;
        this.auditSupport = auditSupport;
    }

    @Tool(description = "Add a resolved dish to the current user's cart. Only use after dishId and quantity are confirmed. 加入购物车，不能创建订单或支付。")
    public CartSnapshot addToCart(AddToCartRequest request) {
        return auditSupport.record("addToCart", summarize(request), () ->
            cartService.addToCart(
                CartContextHolder.cartId(),
                request.dishId(),
                request.quantity(),
                request.remark(),
                request.operationId()
            ));
    }

    @Tool(description = "Set final quantity for an item already in the current user's cart. 修改购物车商品最终数量。")
    public CartSnapshot updateCartItem(UpdateCartItemRequest request) {
        return auditSupport.record("updateCartItem", summarize(request), () ->
            cartService.updateCartItem(
                CartContextHolder.cartId(),
                request.dishId(),
                request.quantity(),
                request.operationId()
            ));
    }

    @Tool(description = "Remove a dish from the current user's cart. 移除购物车商品。")
    public CartSnapshot removeCartItem(RemoveCartItemRequest request) {
        return auditSupport.record("removeCartItem", summarize(request), () ->
            cartService.removeCartItem(CartContextHolder.cartId(), request.dishId(), request.operationId()));
    }

    @Tool(description = "Read the current user's cart snapshot. 查询购物车。")
    public CartSnapshot getCart() {
        return auditSupport.record("getCart", "cartId=" + CartContextHolder.cartId(), () ->
            cartService.getCart(CartContextHolder.cartId()));
    }

    public static class AddToCartRequest {
        private Long dishId;
        private Integer quantity;
        private String remark;
        private String operationId;

        public AddToCartRequest() {
        }

        public AddToCartRequest(Long dishId, Integer quantity, String remark, String operationId) {
            this.dishId = dishId;
            this.quantity = quantity;
            this.remark = remark;
            this.operationId = operationId;
        }

        public Long dishId() {
            return dishId;
        }

        public Integer quantity() {
            return quantity;
        }

        public String remark() {
            return remark;
        }

        public String operationId() {
            return operationId;
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

        public String getRemark() {
            return remark;
        }

        public void setRemark(String remark) {
            this.remark = remark;
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }
    }

    public static class UpdateCartItemRequest {
        private Long dishId;
        private Integer quantity;
        private String operationId;

        public UpdateCartItemRequest() {
        }

        public UpdateCartItemRequest(Long dishId, Integer quantity, String operationId) {
            this.dishId = dishId;
            this.quantity = quantity;
            this.operationId = operationId;
        }

        public Long dishId() {
            return dishId;
        }

        public Integer quantity() {
            return quantity;
        }

        public String operationId() {
            return operationId;
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

    public static class RemoveCartItemRequest {
        private Long dishId;
        private String operationId;

        public RemoveCartItemRequest() {
        }

        public RemoveCartItemRequest(Long dishId, String operationId) {
            this.dishId = dishId;
            this.operationId = operationId;
        }

        public Long dishId() {
            return dishId;
        }

        public String operationId() {
            return operationId;
        }

        public Long getDishId() {
            return dishId;
        }

        public void setDishId(Long dishId) {
            this.dishId = dishId;
        }

        public String getOperationId() {
            return operationId;
        }

        public void setOperationId(String operationId) {
            this.operationId = operationId;
        }
    }

    private String summarize(AddToCartRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "dishId=" + request.dishId()
            + ", quantity=" + request.quantity()
            + ", operationId=" + request.operationId();
    }

    private String summarize(UpdateCartItemRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "dishId=" + request.dishId()
            + ", quantity=" + request.quantity()
            + ", operationId=" + request.operationId();
    }

    private String summarize(RemoveCartItemRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "dishId=" + request.dishId() + ", operationId=" + request.operationId();
    }
}
