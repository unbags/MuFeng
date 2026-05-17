package com.unbags.ordering.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.unbags.ordering.ai.rag.KnowledgeRefreshService;
import com.unbags.ordering.domain.Category;
import com.unbags.ordering.domain.CustomerOrder;
import com.unbags.ordering.domain.Dish;
import com.unbags.ordering.domain.OrderItem;
import com.unbags.ordering.domain.OrderStatusLog;
import com.unbags.ordering.dto.AdminCategoryRequest;
import com.unbags.ordering.dto.AdminDashboardResponse;
import com.unbags.ordering.dto.AdminDishRequest;
import com.unbags.ordering.dto.AdminDishResponse;
import com.unbags.ordering.dto.CategoryResponse;
import com.unbags.ordering.dto.ProductSalesItem;
import com.unbags.ordering.dto.OrderDetailItemResponse;
import com.unbags.ordering.dto.OrderDetailResponse;
import com.unbags.ordering.dto.OrderSummaryResponse;
import com.unbags.ordering.enums.OrderStatus;
import com.unbags.ordering.mapper.CategoryMapper;
import com.unbags.ordering.mapper.CustomerOrderMapper;
import com.unbags.ordering.mapper.DishMapper;
import com.unbags.ordering.mapper.OrderItemMapper;
import com.unbags.ordering.mapper.OrderStatusLogMapper;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.unbags.ordering.dto.PageResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;
    private final CustomerOrderMapper customerOrderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper orderStatusLogMapper;
    private final MenuService menuService;
    private final KnowledgeRefreshService knowledgeRefreshService;
    private final SnowflakeIdGenerator idGenerator;
    private final NotificationService notificationService;
    private final Path imageRoot;

    public AdminService(
        DishMapper dishMapper,
        CategoryMapper categoryMapper,
        CustomerOrderMapper customerOrderMapper,
        OrderItemMapper orderItemMapper,
        OrderStatusLogMapper orderStatusLogMapper,
        MenuService menuService,
        KnowledgeRefreshService knowledgeRefreshService,
        SnowflakeIdGenerator idGenerator,
        NotificationService notificationService,
        @Value("${app.storage.image-dir:src/main/resources/images}") String imageDir
    ) {
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
        this.customerOrderMapper = customerOrderMapper;
        this.orderItemMapper = orderItemMapper;
        this.orderStatusLogMapper = orderStatusLogMapper;
        this.menuService = menuService;
        this.knowledgeRefreshService = knowledgeRefreshService;
        this.idGenerator = idGenerator;
        this.notificationService = notificationService;
        this.imageRoot = Paths.get(imageDir).toAbsolutePath().normalize();
    }

    /**
     * 查询后台分类列表，并转换为前端需要的分类响应。
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories() {
        return listCategories().stream().map(this::toCategoryResponse).collect(Collectors.toList());
    }

    /**
     * 创建分类，并在创建成功后刷新菜单缓存。
     */
    @Transactional
    public CategoryResponse createCategory(AdminCategoryRequest request) {
        String label = request.getLabel().trim();
        String id = buildCategoryId(label);
        if (categoryMapper.selectById(id) != null) {
            throw new IllegalArgumentException("分类已存在");
        }

        Category category = new Category();
        category.setId(id);
        category.setLabel(label);
        category.setSortOrder(request.getSortOrder());
        categoryMapper.insert(category);
        afterMenuMutation();
        return toCategoryResponse(categoryMapper.selectById(id));
    }

    /**
     * 更新分类名称和排序，并刷新菜单缓存。
     */
    @Transactional
    public CategoryResponse updateCategory(String categoryId, AdminCategoryRequest request) {
        Category category = getCategoryOrThrow(categoryId);
        category.setLabel(request.getLabel().trim());
        category.setSortOrder(request.getSortOrder());
        categoryMapper.updateById(category);
        afterMenuMutation();
        return toCategoryResponse(categoryMapper.selectById(category.getId()));
    }

    /**
     * 删除未被有效菜品占用的分类，并迁移已删除菜品的历史分类引用。
     */
    @Transactional
    public void deleteCategory(String categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        if ("all".equalsIgnoreCase(category.getId())) {
            throw new IllegalArgumentException("默认分类不能删除");
        }

        Long dishCount = dishMapper.selectCount(
            new LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, category.getId())
                .eq(Dish::getDeleted, 0)
        );
        if (dishCount != null && dishCount > 0) {
            throw new IllegalArgumentException("该分类下仍有商品，请先调整商品后再删除");
        }

        reassignDeletedDishesBeforeCategoryDelete(category.getId());
        categoryMapper.deleteById(category.getId());
        afterMenuMutation();
    }

    /**
     * 查询后台菜品列表，包含分类名称、上下架和库存信息。
     */
    @Transactional(readOnly = true)
    public List<AdminDishResponse> getDishes() {
        Map<String, String> categoryLabels = buildCategoryLabelMap();
        return dishMapper.selectList(
            new LambdaQueryWrapper<Dish>()
                .eq(Dish::getDeleted, 0)
                .orderByAsc(Dish::getId)
        ).stream().map(dish -> toAdminDishResponse(dish, categoryLabels)).collect(Collectors.toList());
    }

    /**
     * 分页查询后台订单列表，并根据可选条件进行筛选。
     */
    @Transactional(readOnly = true)
    public PageResponse<OrderSummaryResponse> getOrders(
        int page,
        int size,
        String status,
        String orderType,
        String tableNumber,
        String keyword
    ) {
        int safePage = Math.max(1, page);
        int safeSize = Math.min(Math.max(1, size), 100);
        Page<CustomerOrder> pageParam = new Page<>(safePage, safeSize);
        LambdaQueryWrapper<CustomerOrder> wrapper = new LambdaQueryWrapper<CustomerOrder>()
            .orderByDesc(CustomerOrder::getCreatedAt);

        String safeStatus = trimToNull(status);
        if (safeStatus != null && !"all".equalsIgnoreCase(safeStatus)) {
            wrapper.eq(CustomerOrder::getStatus, safeStatus.toUpperCase());
        }
        String safeOrderType = trimToNull(orderType);
        if (safeOrderType != null && !"all".equalsIgnoreCase(safeOrderType)) {
            wrapper.eq(CustomerOrder::getOrderType, safeOrderType);
        }
        String safeTableNumber = trimToNull(tableNumber);
        if (safeTableNumber != null) {
            wrapper.eq(CustomerOrder::getTableNumber, safeTableNumber);
        }
        String safeKeyword = trimToNull(keyword);
        if (safeKeyword != null) {
            wrapper.and(w -> w
                .like(CustomerOrder::getOrderNo, safeKeyword)
                .or()
                .like(CustomerOrder::getPickupNumber, safeKeyword)
                .or()
                .like(CustomerOrder::getContactPhone, safeKeyword)
            );
        }

        Page<CustomerOrder> resultPage = customerOrderMapper.selectPage(pageParam, wrapper);

        List<OrderSummaryResponse> items = resultPage.getRecords().stream()
            .map(this::toOrderSummaryResponse)
            .collect(Collectors.toList());

        return new PageResponse<>(items, resultPage.getTotal(), resultPage.getCurrent(), resultPage.getSize());
    }

    /**
     * 根据订单号查询后台订单详情和明细菜品。
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(String orderNo) {
        CustomerOrder order = customerOrderMapper.selectOne(
            new LambdaQueryWrapper<CustomerOrder>().eq(CustomerOrder::getOrderNo, orderNo).last("limit 1")
        );
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        List<OrderItem> orderItems = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId())
                .orderByAsc(OrderItem::getId)
        );

        List<Long> dishIds = orderItems.stream().map(OrderItem::getDishId).distinct().collect(Collectors.toList());
        Map<Long, Dish> dishMap = dishMapper.selectList(
            new LambdaQueryWrapper<Dish>().in(Dish::getId, dishIds)
        ).stream().collect(Collectors.toMap(Dish::getId, d -> d, (a, b) -> a));

        List<OrderDetailItemResponse> items = orderItems.stream()
            .map(item -> toOrderDetailItemResponse(item, dishMap))
            .collect(Collectors.toList());

        OrderDetailResponse response = new OrderDetailResponse();
        response.setOrderNo(order.getOrderNo());
        response.setOrderType(order.getOrderType());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setNote(order.getNote());
        response.setTableNumber(order.getTableNumber());
        response.setPickupNumber(order.getPickupNumber());
        response.setContactName(order.getContactName());
        response.setContactPhone(order.getContactPhone());
        response.setCancelReason(order.getCancelReason());
        response.setItemCount(order.getItemCount());
        response.setSubtotal(order.getSubtotal());
        response.setPackageFee(BigDecimal.ZERO);
        response.setDeliveryFee(order.getDeliveryFee());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());
        response.setAcceptedAt(order.getAcceptedAt());
        response.setPreparingAt(order.getPreparingAt());
        response.setReadyAt(order.getReadyAt());
        response.setCompletedAt(order.getCompletedAt());
        response.setCancelledAt(order.getCancelledAt());
        response.setItems(items);
        return response;
    }

    /**
     * 更新订单状态，不记录额外原因和操作人。
     */
    @Transactional
    public OrderDetailResponse updateOrderStatus(String orderNo, String newStatus) {
        return updateOrderStatus(orderNo, newStatus, null, null);
    }

    /**
     * 更新订单状态，校验状态流转合法性，并记录状态变更日志。
     */
    @Transactional
    public OrderDetailResponse updateOrderStatus(String orderNo, String newStatus, String reason, String operator) {
        CustomerOrder order = customerOrderMapper.selectOne(
            new LambdaQueryWrapper<CustomerOrder>().eq(CustomerOrder::getOrderNo, orderNo).last("limit 1")
        );
        if (order == null) {
            throw new IllegalArgumentException("订单不存在");
        }

        OrderStatus current = OrderStatus.fromString(order.getStatus());
        OrderStatus target = OrderStatus.fromString(newStatus);

        if (!current.canTransitionTo(target)) {
            throw new IllegalArgumentException("订单状态不能从 " + current.name() + " 变更为 " + target.name());
        }

        order.setStatus(target.name());
        applyStatusTimestamp(order, target, reason);
        customerOrderMapper.updateById(order);
        recordStatusLog(order, current.name(), target.name(), reason, operator);
        OrderDetailResponse updatedOrder = getOrderDetail(orderNo);
        notificationService.notifyOrderStatusChanged(updatedOrder);
        return updatedOrder;
    }

    /**
     * 汇总后台仪表盘经营数据，包括收入、订单数和菜品上下架数量。
     */
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        BigDecimal totalRevenue = customerOrderMapper.sumTotalRevenue();
        if (totalRevenue == null) {
            totalRevenue = BigDecimal.ZERO;
        }

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        BigDecimal todayRevenue = customerOrderMapper.sumTodayRevenue(todayStart);
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }

        Integer todayOrderCount = customerOrderMapper.countTodayOrders(todayStart);
        if (todayOrderCount == null) {
            todayOrderCount = 0;
        }

        Long availableDishCount = dishMapper.selectCount(
            new LambdaQueryWrapper<Dish>().eq(Dish::getDeleted, 0).eq(Dish::getAvailable, Boolean.TRUE)
        );
        Long unavailableDishCount = dishMapper.selectCount(
            new LambdaQueryWrapper<Dish>().eq(Dish::getDeleted, 0).eq(Dish::getAvailable, Boolean.FALSE)
        );

        AdminDashboardResponse response = new AdminDashboardResponse();
        response.setTotalRevenue(totalRevenue);
        response.setTodayRevenue(todayRevenue);
        response.setTodayOrderCount(todayOrderCount);
        response.setAvailableDishCount(availableDishCount == null ? 0L : availableDishCount);
        response.setUnavailableDishCount(unavailableDishCount == null ? 0L : unavailableDishCount);
        return response;
    }

    /**
     * 按周、月或年统计菜品销量和销售额排行。
     */
    @Transactional(readOnly = true)
    public List<ProductSalesItem> getProductSales(String range) {
        int days;
        switch (range != null ? range.toLowerCase() : "week") {
            case "month":
                days = 30;
                break;
            case "year":
                days = 365;
                break;
            default:
                days = 7;
                break;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(days);

        List<CustomerOrder> orders = customerOrderMapper.selectList(
            new LambdaQueryWrapper<CustomerOrder>()
                .ge(CustomerOrder::getCreatedAt, cutoff)
        );

        if (orders.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> orderIds = orders.stream()
            .map(CustomerOrder::getId)
            .collect(Collectors.toList());

        List<OrderItem> items = orderItemMapper.selectList(
            new LambdaQueryWrapper<OrderItem>()
                .in(OrderItem::getOrderId, orderIds)
        );

        return items.stream()
            .collect(Collectors.toMap(
                OrderItem::getDishId,
                item -> new ProductSalesItem(item.getDishId(), item.getDishName(), item.getQuantity(), item.getLineTotal()),
                (a, b) -> {
                    a.setQuantity(a.getQuantity() + b.getQuantity());
                    a.setRevenue(a.getRevenue().add(b.getRevenue()));
                    return a;
                }
            ))
            .values()
            .stream()
            .sorted((a, b) -> b.getQuantity().compareTo(a.getQuantity()))
            .collect(Collectors.toList());
    }

    /**
     * 校验并保存菜品图片，返回图片静态访问路径。
     */
    public String uploadDishImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请选择图片文件");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过五兆");
        }

        String originalName = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String extension = originalName.contains(".") ? originalName.substring(originalName.lastIndexOf('.')) : "";
        if (!Arrays.asList(".png", ".jpg", ".jpeg", ".webp").contains(extension)) {
            throw new IllegalArgumentException("仅支持常见图片格式上传");
        }

        // 校验文件头，避免伪装扩展名上传非图片文件。
        byte[] header = new byte[8];
        try (InputStream is = file.getInputStream()) {
            int bytesRead = is.read(header);
            if (bytesRead < 3) {
                throw new IllegalArgumentException("不支持的文件类型");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("无法读取文件");
        }

        boolean isPng = header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
        boolean isJpeg = header[0] == (byte) 0xFF && header[1] == (byte) 0xD8 && header[2] == (byte) 0xFF;
        boolean isWebp = header[0] == 0x52 && header[1] == 0x49 && header[2] == 0x46 && header[3] == 0x46;

        if (!isPng && !isJpeg && !isWebp) {
            throw new IllegalArgumentException("不支持的文件类型");
        }

        // 校验图片是否能被正常解码读取。
        try (InputStream is = file.getInputStream()) {
            BufferedImage image = ImageIO.read(is);
            if (image == null) {
                throw new IllegalArgumentException("无效的图片文件");
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("无效的图片文件");
        }

        try {
            Files.createDirectories(imageRoot);
            String safeExtension = isPng ? ".png" : isJpeg ? ".jpg" : ".webp";
            String fileName = UUID.randomUUID().toString().replace("-", "") + safeExtension;
            Path target = imageRoot.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/images/" + fileName;
        } catch (IOException exception) {
            throw new IllegalStateException("图片保存失败", exception);
        }
    }

    /**
     * 创建菜品并刷新菜单缓存。
     */
    @Transactional
    public AdminDishResponse createDish(AdminDishRequest request) {
        validateCategory(request.getCategoryId());
        Dish dish = new Dish();
        dish.setId(idGenerator.nextId());
        applyDishRequest(dish, request);
        dish.setDeleted(0);
        dishMapper.insert(dish);
        afterMenuMutation();
        return toAdminDishResponse(dishMapper.selectById(dish.getId()), buildCategoryLabelMap());
    }

    /**
     * 更新菜品资料并刷新菜单缓存。
     */
    @Transactional
    public AdminDishResponse updateDish(Long dishId, AdminDishRequest request) {
        validateCategory(request.getCategoryId());
        Dish dish = getDishOrThrow(dishId);
        applyDishRequest(dish, request);
        dishMapper.updateById(dish);
        afterMenuMutation();
        return toAdminDishResponse(dishMapper.selectById(dishId), buildCategoryLabelMap());
    }

    /**
     * 更新菜品上下架状态并刷新菜单缓存。
     */
    @Transactional
    public AdminDishResponse updateAvailability(Long dishId, Boolean available) {
        if (available == null) {
            throw new IllegalArgumentException("上下架状态不能为空");
        }
        Dish dish = getDishOrThrow(dishId);
        dish.setAvailable(available);
        dishMapper.updateById(dish);
        afterMenuMutation();
        return toAdminDishResponse(dishMapper.selectById(dishId), buildCategoryLabelMap());
    }

    /**
     * 软删除菜品并刷新菜单缓存。
     */
    @Transactional
    public void deleteDish(Long dishId) {
        Dish dish = getDishOrThrow(dishId);
        dish.setAvailable(Boolean.FALSE);
        dishMapper.updateById(dish);
        dishMapper.deleteById(dishId);
        afterMenuMutation();
    }

    private void applyDishRequest(Dish dish, AdminDishRequest request) {
        dish.setName(request.getName().trim());
        dish.setCategoryId(request.getCategoryId().trim());
        dish.setPrice(request.getPrice());
        dish.setRating(request.getRating());
        dish.setCalories(request.getCalories());
        dish.setDescription(request.getDescription().trim());
        dish.setHighlight(request.getHighlight().trim());
        dish.setImageUrl(request.getImageUrl() == null || request.getImageUrl().trim().isEmpty()
            ? null
            : request.getImageUrl().trim());
        dish.setAvailable(request.getAvailable());
        dish.setStock(request.getStock() == null ? -1 : request.getStock());
        if (dish.getDeleted() == null) {
            dish.setDeleted(0);
        }
    }

    private void afterMenuMutation() {
        Runnable refreshTask = () -> {
            menuService.invalidateMenuCache();
            KnowledgeRefreshService.RefreshResult result = knowledgeRefreshService.refreshAllKnowledge();
            if (result.refreshed()) {
                log.info("Knowledge base refreshed after menu change: {} documents", result.documentCount());
            } else if (result.failed()) {
                log.warn("Knowledge base refresh failed after menu change: {}", result.message());
            } else {
                log.debug("Knowledge base refresh skipped after menu change: {}", result.message());
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    refreshTask.run();
                }
            });
            return;
        }
        refreshTask.run();
    }

    private Dish getDishOrThrow(Long dishId) {
        Dish dish = dishMapper.selectOne(
            new LambdaQueryWrapper<Dish>()
                .eq(Dish::getId, dishId)
                .eq(Dish::getDeleted, 0)
                .last("limit 1")
        );
        if (dish == null) {
            throw new IllegalArgumentException("商品不存在");
        }
        return dish;
    }

    private void validateCategory(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty() || "all".equalsIgnoreCase(categoryId.trim())) {
            throw new IllegalArgumentException("分类不存在");
        }
        Category category = categoryMapper.selectById(categoryId.trim());
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
    }

    private Map<String, String> buildCategoryLabelMap() {
        return listCategories().stream()
            .collect(Collectors.toMap(Category::getId, Category::getLabel, (left, right) -> left, HashMap::new));
    }

    private void reassignDeletedDishesBeforeCategoryDelete(String categoryId) {
        String fallbackCategoryId = listCategories().stream()
            .map(Category::getId)
            .filter(id -> !categoryId.equals(id))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("至少保留一个分类"));

        dishMapper.reassignDeletedDishesCategory(categoryId, fallbackCategoryId);
    }

    private List<Category> listCategories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().orderByAsc(Category::getSortOrder, Category::getId));
    }

    private Category getCategoryOrThrow(String categoryId) {
        if (categoryId == null || categoryId.trim().isEmpty()) {
            throw new IllegalArgumentException("分类不存在");
        }
        Category category = categoryMapper.selectById(categoryId.trim());
        if (category == null) {
            throw new IllegalArgumentException("分类不存在");
        }
        return category;
    }

    private String buildCategoryId(String label) {
        String base = label.toLowerCase()
            .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-")
            .replaceAll("^-|-$", "");
        if (base.isEmpty()) {
            base = "category";
        }
        String id = base.length() > 28 ? base.substring(0, 28) : base;
        String candidate = id;
        int index = 2;
        while (categoryMapper.selectById(candidate) != null) {
            String suffix = "-" + index;
            int maxBaseLength = Math.max(1, 32 - suffix.length());
            candidate = id.substring(0, Math.min(id.length(), maxBaseLength)) + suffix;
            index += 1;
        }
        return candidate;
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getLabel(), category.getSortOrder());
    }

    private AdminDishResponse toAdminDishResponse(Dish dish, Map<String, String> categoryLabelMap) {
        AdminDishResponse response = new AdminDishResponse();
        response.setId(dish.getId());
        response.setName(dish.getName());
        response.setCategoryId(dish.getCategoryId());
        response.setCategoryLabel(categoryLabelMap.getOrDefault(dish.getCategoryId(), dish.getCategoryId()));
        response.setPrice(dish.getPrice());
        response.setRating(dish.getRating());
        response.setCalories(dish.getCalories());
        response.setDescription(dish.getDescription());
        response.setHighlight(dish.getHighlight());
        response.setImageUrl(dish.getImageUrl());
        response.setAvailable(Boolean.TRUE.equals(dish.getAvailable()));
        response.setStock(dish.getStock());
        return response;
    }

    private OrderSummaryResponse toOrderSummaryResponse(CustomerOrder order) {
        OrderSummaryResponse response = new OrderSummaryResponse();
        response.setOrderNo(order.getOrderNo());
        response.setOrderType(order.getOrderType());
        response.setStatus(order.getStatus());
        response.setPaymentStatus(order.getPaymentStatus());
        response.setTableNumber(order.getTableNumber());
        response.setPickupNumber(order.getPickupNumber());
        response.setContactName(order.getContactName());
        response.setContactPhone(order.getContactPhone());
        response.setItemCount(order.getItemCount());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }

    private void applyStatusTimestamp(CustomerOrder order, OrderStatus target, String reason) {
        LocalDateTime now = LocalDateTime.now();
        switch (target) {
            case PREPARING:
                order.setPreparingAt(now);
                break;
            case COMPLETED:
                order.setCompletedAt(now);
                break;
            case CANCELLED:
                order.setCancelledAt(now);
                order.setCancelReason(trimToNull(reason));
                break;
            default:
                break;
        }
    }

    private void recordStatusLog(CustomerOrder order, String fromStatus, String toStatus, String reason, String operator) {
        OrderStatusLog log = new OrderStatusLog();
        log.setId(idGenerator.nextId());
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setReason(trimToNull(reason));
        log.setOperator(trimToNull(operator));
        orderStatusLogMapper.insert(log);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private OrderDetailItemResponse toOrderDetailItemResponse(OrderItem item, Map<Long, Dish> dishMap) {
        OrderDetailItemResponse response = new OrderDetailItemResponse();
        response.setDishId(item.getDishId());
        response.setName(item.getDishName());
        response.setPrice(item.getDishPrice());
        response.setQuantity(item.getQuantity());
        response.setTotal(item.getLineTotal());
        Dish dish = dishMap.get(item.getDishId());
        if (dish != null) {
            response.setImageUrl(dish.getImageUrl());
        }
        return response;
    }
}
