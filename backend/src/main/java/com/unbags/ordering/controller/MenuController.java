package com.unbags.ordering.controller;

import com.unbags.ordering.dto.ApiResponse;
import com.unbags.ordering.dto.MenuResponse;
import com.unbags.ordering.service.MenuService;
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
