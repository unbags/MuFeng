package com.unbags.ordering.service;

import com.unbags.ordering.dto.CartItemResponse;
import com.unbags.ordering.dto.CartSnapshot;
import com.unbags.ordering.dto.DishResponse;
import com.unbags.ordering.dto.MenuResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CartService {

    private static final int MAX_ADD_QUANTITY = 20;
    private static final int MAX_ITEM_QUANTITY = 99;

    private final MenuService menuService;
    private final Map<String, CartState> carts = new ConcurrentHashMap<>();

    public CartService(MenuService menuService) {
        this.menuService = menuService;
    }

    public CartSnapshot getCart(String cartId) {
        CartState state = state(cartId);
        synchronized (state) {
            return snapshot(cartId, state);
        }
    }

    public CartSnapshot addToCart(String cartId, Long dishId, Integer quantity, String remark, String operationId) {
        validateQuantity(quantity, MAX_ADD_QUANTITY);
        DishResponse dish = requireDish(dishId);
        CartState state = state(cartId);
        synchronized (state) {
            if (state.hasOperation(operationId)) {
                return snapshot(cartId, state);
            }
            CartEntry entry = state.items.computeIfAbsent(dish.getId(), id -> new CartEntry(dish.getId()));
            int nextQuantity = entry.quantity + quantity;
            if (nextQuantity > MAX_ITEM_QUANTITY) {
                throw new IllegalArgumentException("商品数量过大，请分次操作");
            }
            entry.quantity = nextQuantity;
            entry.remark = trimToNull(remark) != null ? trimToNull(remark) : entry.remark;
            state.markOperation(operationId);
            return snapshot(cartId, state);
        }
    }

    public CartSnapshot updateCartItem(String cartId, Long dishId, Integer quantity, String operationId) {
        validateQuantity(quantity, MAX_ITEM_QUANTITY);
        requireDish(dishId);
        CartState state = state(cartId);
        synchronized (state) {
            if (state.hasOperation(operationId)) {
                return snapshot(cartId, state);
            }
            CartEntry entry = state.items.get(dishId);
            if (entry == null) {
                throw new IllegalArgumentException("购物车中没有该商品");
            }
            entry.quantity = quantity;
            state.markOperation(operationId);
            return snapshot(cartId, state);
        }
    }

    public CartSnapshot removeCartItem(String cartId, Long dishId, String operationId) {
        CartState state = state(cartId);
        synchronized (state) {
            if (state.hasOperation(operationId)) {
                return snapshot(cartId, state);
            }
            state.items.remove(dishId);
            state.markOperation(operationId);
            return snapshot(cartId, state);
        }
    }

    public CartSnapshot clearCart(String cartId) {
        CartState state = state(cartId);
        synchronized (state) {
            state.items.clear();
            state.operations.clear();
            return snapshot(cartId, state);
        }
    }

    private CartState state(String cartId) {
        String key = normalizeCartId(cartId);
        return carts.computeIfAbsent(key, ignored -> new CartState());
    }

    private CartSnapshot snapshot(String cartId, CartState state) {
        Map<Long, DishResponse> dishes = currentDishMap();
        List<CartItemResponse> items = state.items.values().stream()
            .sorted(Comparator.comparing(CartEntry::dishId))
            .map(entry -> toResponse(entry, dishes.get(entry.dishId)))
            .collect(Collectors.toCollection(ArrayList::new));
        BigDecimal total = items.stream()
            .map(CartItemResponse::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        int itemCount = items.stream().mapToInt(item -> item.getQuantity() == null ? 0 : item.getQuantity()).sum();
        return new CartSnapshot(normalizeCartId(cartId), items, itemCount, total);
    }

    private CartItemResponse toResponse(CartEntry entry, DishResponse dish) {
        BigDecimal price = dish == null || dish.getPrice() == null ? BigDecimal.ZERO : dish.getPrice();
        BigDecimal lineTotal = price.multiply(BigDecimal.valueOf(entry.quantity));
        return new CartItemResponse(
            entry.dishId,
            dish == null ? "未知商品" : dish.getName(),
            dish == null ? null : dish.getCategory(),
            price,
            entry.quantity,
            lineTotal,
            entry.remark,
            dish == null ? null : dish.getImageUrl(),
            dish == null ? null : dish.getDescription(),
            dish == null ? null : dish.getHighlight()
        );
    }

    private DishResponse requireDish(Long dishId) {
        if (dishId == null) {
            throw new IllegalArgumentException("商品编号不能为空");
        }
        DishResponse dish = currentDishMap().get(dishId);
        if (dish == null) {
            throw new IllegalArgumentException("商品不存在或已下架");
        }
        return dish;
    }

    private Map<Long, DishResponse> currentDishMap() {
        MenuResponse menu = menuService.getMenu();
        if (menu.getDishes() == null) {
            return Map.of();
        }
        return menu.getDishes().stream()
            .filter(dish -> dish.getId() != null)
            .collect(Collectors.toMap(DishResponse::getId, dish -> dish, (left, right) -> left, LinkedHashMap::new));
    }

    private void validateQuantity(Integer quantity, int max) {
        if (quantity == null || quantity < 1) {
            throw new IllegalArgumentException("商品数量至少为1");
        }
        if (quantity > max) {
            throw new IllegalArgumentException("商品数量过大，请分次操作");
        }
    }

    private String normalizeCartId(String cartId) {
        String trimmed = trimToNull(cartId);
        return trimmed == null ? "anonymous" : trimmed;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static class CartState {
        private final Map<Long, CartEntry> items = new LinkedHashMap<>();
        private final List<String> operations = new ArrayList<>();

        private boolean hasOperation(String operationId) {
            return operationId != null && operations.contains(operationId);
        }

        private void markOperation(String operationId) {
            if (operationId == null || operationId.trim().isEmpty()) {
                return;
            }
            operations.add(operationId);
            if (operations.size() > 200) {
                operations.remove(0);
            }
        }
    }

    private static class CartEntry {
        private final Long dishId;
        private int quantity;
        private String remark;

        private CartEntry(Long dishId) {
            this.dishId = dishId;
        }

        private Long dishId() {
            return dishId;
        }
    }
}
