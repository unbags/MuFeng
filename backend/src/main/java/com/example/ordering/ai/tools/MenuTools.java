package com.example.ordering.ai.tools;

import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.service.MenuService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MenuTools {

    private final MenuService menuService;

    public MenuTools(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 查询当前可售菜单，供 AI 助手回答菜单类问题。
     */
    @Tool(description = "查询当前可售菜单和菜品信息。适用于用户询问当前有哪些菜、价格、分类或推荐候选。")
    public MenuToolResponse getMenu() {
        return new MenuToolResponse(currentDishes().stream().map(this::toToolItem).toList());
    }

    /**
     * 按关键词、分类和预算搜索当前可售菜品。
     */
    @Tool(description = "按关键词、分类和最高预算搜索当前可售菜品。不会返回已下架菜品。")
    public DishSearchResponse searchDishes(DishSearchRequest request) {
        String keyword = normalize(request.keyword());
        String category = normalize(request.category());

        List<DishToolItem> matches = currentDishes().stream()
            .filter(dish -> keyword == null || contains(dish.getName(), keyword) || contains(dish.getHighlight(), keyword))
            .filter(dish -> category == null || category.equalsIgnoreCase(nullToEmpty(dish.getCategory())))
            .filter(dish -> request.maxPrice() == null || dish.getPrice() == null || dish.getPrice().compareTo(request.maxPrice()) <= 0)
            .sorted(Comparator.comparing(DishResponse::getPrice, Comparator.nullsLast(Comparator.naturalOrder())))
            .map(this::toToolItem)
            .toList();

        return new DishSearchResponse(matches);
    }

    /**
     * 获取当前可售菜品列表。
     */
    private List<DishResponse> currentDishes() {
        MenuResponse menu = menuService.getMenu();
        return menu.getDishes() == null ? List.of() : menu.getDishes();
    }

    /**
     * 将菜单菜品响应转换为 AI 工具返回项。
     */
    private DishToolItem toToolItem(DishResponse dish) {
        return new DishToolItem(dish.getId(), dish.getName(), dish.getCategory(), dish.getPrice(), dish.getHighlight());
    }

    /**
     * 判断文本是否包含关键词。
     */
    private boolean contains(String value, String keyword) {
        return value != null && value.toLowerCase().contains(keyword);
    }

    /**
     * 统一清洗查询条件。
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    /**
     * 将 null 字符串转换为空字符串。
     */
    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
