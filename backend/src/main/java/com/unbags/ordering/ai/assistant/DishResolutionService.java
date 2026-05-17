package com.unbags.ordering.ai.assistant;

import com.unbags.ordering.dto.DishResponse;
import com.unbags.ordering.dto.MenuResponse;
import com.unbags.ordering.service.MenuService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishResolutionService {

    private final MenuService menuService;

    public DishResolutionService(MenuService menuService) {
        this.menuService = menuService;
    }

    public DishResolutionResult resolve(String rawName) {
        String keyword = normalize(rawName);
        if (keyword == null) {
            return new DishResolutionResult(null, List.of());
        }
        MenuResponse menu = menuService.getMenu();
        List<DishResponse> dishes = menu.getDishes() == null ? List.of() : menu.getDishes();
        List<DishResponse> matches = dishes.stream()
            .filter(dish -> matches(dish, keyword))
            .sorted(Comparator.comparing((DishResponse dish) -> score(dish, keyword)).reversed())
            .limit(5)
            .collect(Collectors.toList());
        if (matches.size() == 1 || (!matches.isEmpty() && exactName(matches.get(0), keyword))) {
            return new DishResolutionResult(matches.get(0), matches);
        }
        return new DishResolutionResult(null, matches);
    }

    private boolean matches(DishResponse dish, String keyword) {
        return contains(dish.getName(), keyword)
            || contains(dish.getDescription(), keyword)
            || contains(dish.getHighlight(), keyword);
    }

    private int score(DishResponse dish, String keyword) {
        if (exactName(dish, keyword)) {
            return 100;
        }
        if (contains(dish.getName(), keyword)) {
            return 80;
        }
        if (contains(keyword, dish.getName())) {
            return 70;
        }
        if (contains(dish.getHighlight(), keyword)) {
            return 40;
        }
        return 10;
    }

    private boolean exactName(DishResponse dish, String keyword) {
        return dish.getName() != null && dish.getName().equalsIgnoreCase(keyword);
    }

    private boolean contains(String value, String keyword) {
        return value != null && keyword != null && value.toLowerCase().contains(keyword.toLowerCase());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
