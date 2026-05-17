package com.unbags.ordering.controller;

import com.unbags.ordering.dto.AdminDashboardResponse;
import com.unbags.ordering.dto.AdminCategoryRequest;
import com.unbags.ordering.dto.AdminDishRequest;
import com.unbags.ordering.dto.AdminDishResponse;
import com.unbags.ordering.dto.ApiResponse;
import com.unbags.ordering.dto.CategoryResponse;
import com.unbags.ordering.dto.ImageUploadResponse;
import com.unbags.ordering.dto.OrderDetailResponse;
import com.unbags.ordering.dto.OrderStatusRequest;
import com.unbags.ordering.dto.PageResponse;
import com.unbags.ordering.dto.ProductSalesItem;
import com.unbags.ordering.dto.OrderSummaryResponse;

import com.unbags.ordering.service.AdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import jakarta.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    /**
     * 查询后台菜品管理列表。
     */
    @GetMapping("/dishes")
    public ApiResponse<List<AdminDishResponse>> getDishes() {
        return ApiResponse.success(adminService.getDishes());
    }

    /**
     * 查询后台可管理的菜品分类列表。
     */
    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(adminService.getCategories());
    }

    /**
     * 创建菜品分类。
     */
    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody AdminCategoryRequest request) {
        return ApiResponse.success(adminService.createCategory(request));
    }

    /**
     * 根据分类编号更新分类名称和排序。
     */
    @PutMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
        @PathVariable String categoryId,
        @Valid @RequestBody AdminCategoryRequest request
    ) {
        return ApiResponse.success(adminService.updateCategory(categoryId, request));
    }

    /**
     * 根据分类编号删除空分类。
     */
    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable String categoryId) {
        adminService.deleteCategory(categoryId);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 上传菜品图片并返回可访问的图片地址。
     */
    @PostMapping("/dishes/upload")
    public ApiResponse<ImageUploadResponse> uploadDishImage(@RequestParam("file") MultipartFile file) {
        String relativeUrl = adminService.uploadDishImage(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(relativeUrl)
            .toUriString();
        return ApiResponse.success(new ImageUploadResponse(url));
    }

    /**
     * 创建后台菜品。
     */
    @PostMapping("/dishes")
    public ApiResponse<AdminDishResponse> createDish(@Valid @RequestBody AdminDishRequest request) {
        return ApiResponse.success(adminService.createDish(request));
    }

    /**
     * 根据菜品编号更新菜品资料。
     */
    @PutMapping("/dishes/{dishId}")
    public ApiResponse<AdminDishResponse> updateDish(
        @PathVariable Long dishId,
        @Valid @RequestBody AdminDishRequest request
    ) {
        return ApiResponse.success(adminService.updateDish(dishId, request));
    }

    /**
     * 根据菜品编号切换菜品上下架状态。
     */
    @PatchMapping("/dishes/{dishId}/availability")
    public ApiResponse<AdminDishResponse> updateAvailability(
        @PathVariable Long dishId,
        @RequestParam Boolean available
    ) {
        return ApiResponse.success(adminService.updateAvailability(dishId, available));
    }

    /**
     * 根据菜品编号删除菜品。
     */
    @DeleteMapping("/dishes/{dishId}")
    public ApiResponse<Void> deleteDish(@PathVariable Long dishId) {
        adminService.deleteDish(dishId);
        return ApiResponse.success("删除成功", null);
    }

    /**
     * 分页查询后台订单列表，支持状态、类型、桌号和关键词筛选。
     */
    @GetMapping("/orders")
    public ApiResponse<PageResponse<OrderSummaryResponse>> getOrders(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String orderType,
        @RequestParam(required = false) String tableNumber,
        @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.success(adminService.getOrders(page, size, status, orderType, tableNumber, keyword));
    }

    /**
     * 根据订单号查询后台订单详情。
     */
    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable String orderNo) {
        return ApiResponse.success(adminService.getOrderDetail(orderNo));
    }

    /**
     * 根据订单号更新订单状态并记录状态变更。
     */
    @PatchMapping("/orders/{orderNo}/status")
    public ApiResponse<OrderDetailResponse> updateOrderStatus(
        @PathVariable String orderNo,
        @Valid @RequestBody OrderStatusRequest request
    ) {
        return ApiResponse.success(adminService.updateOrderStatus(
            orderNo,
            request.getStatus(),
            request.getReason(),
            request.getOperator()
        ));
    }

    /**
     * 查询后台经营数据概览。
     */
    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        return ApiResponse.success(adminService.getDashboard());
    }

    /**
     * 按时间范围查询菜品销量排行。
     */
    @GetMapping("/dashboard/product-sales")
    public ApiResponse<List<ProductSalesItem>> getProductSales(@RequestParam(defaultValue = "week") String range) {
        return ApiResponse.success(adminService.getProductSales(range));
    }
}
