package com.example.ordering.controller;

import com.example.ordering.dto.AdminDashboardResponse;
import com.example.ordering.dto.AdminCategoryRequest;
import com.example.ordering.dto.AdminDishRequest;
import com.example.ordering.dto.AdminDishResponse;
import com.example.ordering.dto.ApiResponse;
import com.example.ordering.dto.CategoryResponse;
import com.example.ordering.dto.ImageUploadResponse;
import com.example.ordering.dto.OrderDetailResponse;
import com.example.ordering.dto.OrderStatusRequest;
import com.example.ordering.dto.PageResponse;
import com.example.ordering.dto.ProductSalesItem;
import com.example.ordering.dto.OrderSummaryResponse;

import com.example.ordering.service.AdminService;
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

    @GetMapping("/dishes")
    public ApiResponse<List<AdminDishResponse>> getDishes() {
        return ApiResponse.success(adminService.getDishes());
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> getCategories() {
        return ApiResponse.success(adminService.getCategories());
    }

    @PostMapping("/categories")
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody AdminCategoryRequest request) {
        return ApiResponse.success(adminService.createCategory(request));
    }

    @PutMapping("/categories/{categoryId}")
    public ApiResponse<CategoryResponse> updateCategory(
        @PathVariable String categoryId,
        @Valid @RequestBody AdminCategoryRequest request
    ) {
        return ApiResponse.success(adminService.updateCategory(categoryId, request));
    }

    @DeleteMapping("/categories/{categoryId}")
    public ApiResponse<Void> deleteCategory(@PathVariable String categoryId) {
        adminService.deleteCategory(categoryId);
        return ApiResponse.success("删除成功", null);
    }

    @PostMapping("/dishes/upload")
    public ApiResponse<ImageUploadResponse> uploadDishImage(@RequestParam("file") MultipartFile file) {
        String relativeUrl = adminService.uploadDishImage(file);
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
            .path(relativeUrl)
            .toUriString();
        return ApiResponse.success(new ImageUploadResponse(url));
    }

    @PostMapping("/dishes")
    public ApiResponse<AdminDishResponse> createDish(@Valid @RequestBody AdminDishRequest request) {
        return ApiResponse.success(adminService.createDish(request));
    }

    @PutMapping("/dishes/{dishId}")
    public ApiResponse<AdminDishResponse> updateDish(
        @PathVariable Long dishId,
        @Valid @RequestBody AdminDishRequest request
    ) {
        return ApiResponse.success(adminService.updateDish(dishId, request));
    }

    @PatchMapping("/dishes/{dishId}/availability")
    public ApiResponse<AdminDishResponse> updateAvailability(
        @PathVariable Long dishId,
        @RequestParam Boolean available
    ) {
        return ApiResponse.success(adminService.updateAvailability(dishId, available));
    }

    @DeleteMapping("/dishes/{dishId}")
    public ApiResponse<Void> deleteDish(@PathVariable Long dishId) {
        adminService.deleteDish(dishId);
        return ApiResponse.success("删除成功", null);
    }

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

    @GetMapping("/orders/{orderNo}")
    public ApiResponse<OrderDetailResponse> getOrderDetail(@PathVariable String orderNo) {
        return ApiResponse.success(adminService.getOrderDetail(orderNo));
    }

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

    @GetMapping("/dashboard")
    public ApiResponse<AdminDashboardResponse> getDashboard() {
        return ApiResponse.success(adminService.getDashboard());
    }

    @GetMapping("/dashboard/product-sales")
    public ApiResponse<List<ProductSalesItem>> getProductSales(@RequestParam(defaultValue = "week") String range) {
        return ApiResponse.success(adminService.getProductSales(range));
    }
}
