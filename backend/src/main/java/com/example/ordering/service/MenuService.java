package com.example.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.config.HighConcurrencyProperties;
import com.example.ordering.domain.Category;
import com.example.ordering.domain.Dish;
import com.example.ordering.dto.CategoryResponse;
import com.example.ordering.dto.DishResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.mapper.CategoryMapper;
import com.example.ordering.mapper.DishMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class MenuService {

    private static final Logger log = LoggerFactory.getLogger(MenuService.class);
    private static final String MENU_CACHE_KEY = "menu:cache";

    private final CategoryMapper categoryMapper;
    private final DishMapper dishMapper;
    private final long menuCacheTtlSeconds;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public MenuService(
        CategoryMapper categoryMapper,
        DishMapper dishMapper,
        HighConcurrencyProperties properties,
        StringRedisTemplate redisTemplate,
        ObjectMapper redisObjectMapper
    ) {
        this.categoryMapper = categoryMapper;
        this.dishMapper = dishMapper;
        this.menuCacheTtlSeconds = properties.getMenuCacheTtlSeconds();
        this.redisTemplate = redisTemplate;
        this.objectMapper = redisObjectMapper;
    }

    /**
     * 查询当前菜单，优先读取 Redis 缓存，缓存缺失时回源数据库。
     */
    public MenuResponse getMenu() {
        MenuResponse cached = getFromRedis();
        if (cached != null) {
            return cached;
        }
        return loadAndCache();
    }

    /**
     * 清理菜单缓存，用于菜品或分类变更后刷新前台展示。
     */
    public void invalidateMenuCache() {
        try {
            redisTemplate.delete(MENU_CACHE_KEY);
        } catch (Exception e) {
            log.warn("Failed to invalidate Redis menu cache", e);
        }
    }

    private MenuResponse getFromRedis() {
        try {
            String json = redisTemplate.opsForValue().get(MENU_CACHE_KEY);
            if (json != null) {
                return objectMapper.readValue(json, MenuResponse.class);
            }
        } catch (Exception e) {
            log.warn("Redis read failed, falling back to database", e);
        }
        return null;
    }

    private synchronized MenuResponse loadAndCache() {
        MenuResponse cached = getFromRedis();
        if (cached != null) {
            return cached;
        }

        MenuResponse response = loadMenuFromDatabase();
        String json;
        try {
            json = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(MENU_CACHE_KEY, json, menuCacheTtlSeconds, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis write failed, menu will be loaded from database on next request", e);
        }
        return response;
    }

    private MenuResponse loadMenuFromDatabase() {
        List<CategoryResponse> categories = categoryMapper.selectList(
            new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder, Category::getId)
        ).stream().map(this::toCategoryResponse).collect(Collectors.toList());

        List<DishResponse> dishes = dishMapper.selectList(
            new LambdaQueryWrapper<Dish>()
                .eq(Dish::getAvailable, Boolean.TRUE)
                .eq(Dish::getDeleted, 0)
                .orderByAsc(Dish::getId)
        ).stream().map(this::toDishResponse).collect(Collectors.toList());

        return new MenuResponse(
            Collections.unmodifiableList(categories),
            Collections.unmodifiableList(dishes)
        );
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getLabel(), category.getSortOrder());
    }

    private DishResponse toDishResponse(Dish dish) {
        DishResponse response = new DishResponse();
        response.setId(dish.getId());
        response.setName(dish.getName());
        response.setCategory(dish.getCategoryId());
        response.setPrice(dish.getPrice());
        response.setRating(dish.getRating());
        response.setCalories(dish.getCalories());
        response.setDescription(dish.getDescription());
        response.setHighlight(dish.getHighlight());
        response.setImageUrl(dish.getImageUrl());
        return response;
    }
}
