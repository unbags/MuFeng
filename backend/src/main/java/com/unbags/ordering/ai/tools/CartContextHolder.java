package com.unbags.ordering.ai.tools;

public final class CartContextHolder {

    private static final ThreadLocal<String> CART_ID = new ThreadLocal<>();

    private CartContextHolder() {
    }

    public static void setCartId(String cartId) {
        CART_ID.set(cartId);
    }

    public static String cartId() {
        String cartId = CART_ID.get();
        return cartId == null || cartId.trim().isEmpty() ? "anonymous-ai" : cartId;
    }

    public static void clear() {
        CART_ID.remove();
    }
}
