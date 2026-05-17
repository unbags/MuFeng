package com.unbags.ordering.controller;

import com.unbags.ordering.dto.ApiResponse;
import com.unbags.ordering.dto.CartItemRequest;
import com.unbags.ordering.dto.CartSnapshot;
import com.unbags.ordering.dto.CartUpdateItemRequest;
import com.unbags.ordering.service.CartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ApiResponse<CartSnapshot> getCart(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        HttpSession session
    ) {
        return ApiResponse.success(cartService.getCart(resolveCartId(cartId, session)));
    }

    @PostMapping("/items")
    public ApiResponse<CartSnapshot> addItem(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        HttpSession session,
        @Valid @RequestBody CartItemRequest request
    ) {
        CartSnapshot snapshot = cartService.addToCart(
            resolveCartId(cartId, session),
            request.getDishId(),
            request.getQuantity(),
            request.getRemark(),
            request.getOperationId()
        );
        return ApiResponse.success(snapshot);
    }

    @PutMapping("/items/{dishId}")
    public ApiResponse<CartSnapshot> updateItem(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        HttpSession session,
        @PathVariable Long dishId,
        @Valid @RequestBody CartUpdateItemRequest request
    ) {
        CartSnapshot snapshot = cartService.updateCartItem(
            resolveCartId(cartId, session),
            dishId,
            request.getQuantity(),
            request.getOperationId()
        );
        return ApiResponse.success(snapshot);
    }

    @DeleteMapping("/items/{dishId}")
    public ApiResponse<CartSnapshot> removeItem(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        HttpSession session,
        @PathVariable Long dishId,
        @RequestParam(value = "operationId", required = false) String operationId
    ) {
        return ApiResponse.success(cartService.removeCartItem(resolveCartId(cartId, session), dishId, operationId));
    }

    @DeleteMapping
    public ApiResponse<CartSnapshot> clearCart(
        @RequestHeader(value = "X-Cart-Id", required = false) String cartId,
        HttpSession session
    ) {
        return ApiResponse.success(cartService.clearCart(resolveCartId(cartId, session)));
    }

    private String resolveCartId(String cartId, HttpSession session) {
        if (cartId != null && !cartId.trim().isEmpty()) {
            return cartId.trim();
        }
        return "session-" + session.getId();
    }
}
