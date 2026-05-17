package com.unbags.ordering.ai.tools;

import com.unbags.ordering.dto.DishResponse;
import com.unbags.ordering.dto.MenuResponse;
import com.unbags.ordering.service.MenuService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecommendTools {

    private final MenuService menuService;
    private final ToolAuditSupport auditSupport;

    public RecommendTools(MenuService menuService) {
        this(menuService, ToolAuditSupport.disabled());
    }

    @Autowired
    public RecommendTools(MenuService menuService, ToolAuditSupport auditSupport) {
        this.menuService = menuService;
        this.auditSupport = auditSupport;
    }

    /**
     * 根据用餐人数、预算、忌口和口味偏好推荐菜品。
     */
    @Tool(description = "Recommend dishes based on party size, budget, dietary restrictions, and taste preferences. Call when user asks for recommendations. 根据人数/预算/忌口/口味推荐菜品。")
    public RecommendResponse recommendDishes(RecommendRequest request) {
        return auditSupport.record("recommendDishes", summarize(request), () -> {
            RecommendRequest safeRequest = request == null ? new RecommendRequest(0, null, null, null) : request;
            List<DishResponse> dishes = currentDishes();
            if (dishes.isEmpty()) {
                return new RecommendResponse(List.of(), "当前菜单暂无菜品，建议稍后再试。");
            }

            String dietary = normalize(safeRequest.dietary());
            String taste = normalize(safeRequest.taste());
            BigDecimal maxBudget = safeRequest.maxBudget();

            List<ScoredDish> scored = new ArrayList<>();
            for (DishResponse dish : dishes) {
                int score = 0;

                if (dietary != null) {
                    if (matches(dish.getDescription(), dietary) || matches(dish.getHighlight(), dietary)) {
                        score += 3;
                    }
                }
                if (taste != null) {
                    if (matches(dish.getDescription(), taste) || matches(dish.getHighlight(), taste)
                        || matches(dish.getCategory(), taste)) {
                        score += 3;
                    }
                }
                if (maxBudget != null && dish.getPrice() != null) {
                    if (dish.getPrice().compareTo(maxBudget) <= 0) {
                        score += 2;
                    }
                }
                if (dish.getRating() != null) {
                    score += dish.getRating().intValue();
                }
                if (dish.getHighlight() != null && !dish.getHighlight().trim().isEmpty()) {
                    score += 1;
                }
                scored.add(new ScoredDish(dish, score));
            }

            scored.sort(Comparator.comparing(ScoredDish::score).reversed());

            List<RecommendItem> items = scored.stream()
                .limit(4)
                .map(sd -> {
                    DishResponse d = sd.dish;
                    StringBuilder reason = new StringBuilder();
                    if (d.getHighlight() != null && !d.getHighlight().trim().isEmpty()) {
                        reason.append(d.getHighlight());
                    }
                    if (sd.score >= 3) {
                        if (reason.length() > 0) reason.append("；");
                        reason.append("匹配您的偏好");
                    }
                    if (d.getPrice() != null) {
                        reason.append("（").append(d.getPrice()).append("元）");
                    }
                    return new RecommendItem(d.getId(), d.getName(), d.getCategory(),
                        d.getPrice(), reason.toString());
                })
                .collect(Collectors.toList());

            String note = safeRequest.partySize() > 0
                ? "基于" + safeRequest.partySize() + "人用餐推荐，请以页面实时价格和库存为准。"
                : "请以页面实时价格和库存为准。";

            return new RecommendResponse(items, note);
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
     * 判断文本是否匹配用户关键词。
     */
    private boolean matches(String text, String keyword) {
        return text != null && text.toLowerCase().contains(keyword);
    }

    /**
     * 统一清洗用户偏好文本。
     */
    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }

    private static class ScoredDish {
        private final DishResponse dish;
        private final int score;

        private ScoredDish(DishResponse dish, int score) {
            this.dish = dish;
            this.score = score;
        }

        private DishResponse dish() {
            return dish;
        }

        private int score() {
            return score;
        }
    }

    public static class RecommendRequest {
        private int partySize;
        private BigDecimal maxBudget;
        private String dietary;
        private String taste;

        public RecommendRequest() {
        }

        public RecommendRequest(int partySize, BigDecimal maxBudget, String dietary, String taste) {
            this.partySize = partySize;
            this.maxBudget = maxBudget;
            this.dietary = dietary;
            this.taste = taste;
        }

        public int partySize() {
            return partySize;
        }

        public BigDecimal maxBudget() {
            return maxBudget;
        }

        public String dietary() {
            return dietary;
        }

        public String taste() {
            return taste;
        }

        public int getPartySize() {
            return partySize;
        }

        public void setPartySize(int partySize) {
            this.partySize = partySize;
        }

        public BigDecimal getMaxBudget() {
            return maxBudget;
        }

        public void setMaxBudget(BigDecimal maxBudget) {
            this.maxBudget = maxBudget;
        }

        public String getDietary() {
            return dietary;
        }

        public void setDietary(String dietary) {
            this.dietary = dietary;
        }

        public String getTaste() {
            return taste;
        }

        public void setTaste(String taste) {
            this.taste = taste;
        }
    }

    public static class RecommendItem {
        private Long id;
        private String name;
        private String category;
        private BigDecimal price;
        private String reason;

        public RecommendItem() {
        }

        public RecommendItem(Long id, String name, String category, BigDecimal price, String reason) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.reason = reason;
        }

        public Long id() {
            return id;
        }

        public String name() {
            return name;
        }

        public String category() {
            return category;
        }

        public BigDecimal price() {
            return price;
        }

        public String reason() {
            return reason;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public BigDecimal getPrice() {
            return price;
        }

        public void setPrice(BigDecimal price) {
            this.price = price;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class RecommendResponse {
        private List<RecommendItem> items;
        private String note;

        public RecommendResponse() {
        }

        public RecommendResponse(List<RecommendItem> items, String note) {
            this.items = items;
            this.note = note;
        }

        public List<RecommendItem> items() {
            return items;
        }

        public String note() {
            return note;
        }

        public List<RecommendItem> getItems() {
            return items;
        }

        public void setItems(List<RecommendItem> items) {
            this.items = items;
        }

        public String getNote() {
            return note;
        }

        public void setNote(String note) {
            this.note = note;
        }
    }

    private String summarize(RecommendRequest request) {
        if (request == null) {
            return "request=null";
        }
        return "partySize=" + request.partySize()
            + ", maxBudget=" + request.maxBudget()
            + ", dietary=" + request.dietary()
            + ", taste=" + request.taste();
    }
}
