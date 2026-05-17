package com.example.ordering.ai.tools;

import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.service.MenuService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class MenuTools {

    private final MenuService menuService;
    private final ToolAuditSupport auditSupport;

    public MenuTools(MenuService menuService) {
        this(menuService, ToolAuditSupport.disabled());
    }

    @Autowired
    public MenuTools(MenuService menuService, ToolAuditSupport auditSupport) {
        this.menuService = menuService;
        this.auditSupport = auditSupport;
    }

    /**
     * 查询当前可售菜单，供 AI 助手回答菜单类问题。
     */
    @Tool(description = "Query the full available menu with all dishes, categories, and prices. Call this when the user asks about menu, dishes, what's available, or what to eat. 查询当前完整可售菜单。")
    public MenuToolResponse getMenu() {
        return auditSupport.record("getMenu", "all", () ->
            new MenuToolResponse(currentDishes().stream().map(this::toToolItem).toList()));
    }

    /**
     * 按关键词、分类和预算搜索当前可售菜品。
     */
    @Tool(description = "Search dishes by keyword, category, and/or max budget. Call when user asks about specific dish types, categories, or has price constraints. 按关键词/分类/预算搜索菜品。")
    public DishSearchResponse searchDishes(DishSearchRequest request) {
        return auditSupport.record("searchDishes", summarize(request), () -> {
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
        });
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

    private String summarize(DishSearchRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "keyword=" + request.keyword()
            + ", category=" + request.category()
            + ", maxPrice=" + request.maxPrice();
    }
}
