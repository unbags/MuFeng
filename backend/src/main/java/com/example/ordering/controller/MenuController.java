package com.example.ordering.controller;

import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.MenuResponse;
import com.example.ordering.service.MenuService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 查询前台菜单分类和当前可售菜品。
     */
    @GetMapping
    public ApiResponse<MenuResponse> getMenu() {
        return ApiResponse.success(menuService.getMenu());
    }
}
