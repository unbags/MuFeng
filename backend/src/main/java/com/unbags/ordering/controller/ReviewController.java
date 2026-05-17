package com.unbags.ordering.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.unbags.ordering.domain.Review;
import com.unbags.ordering.dto.ApiResponse;
import com.unbags.ordering.dto.ReviewRequest;
import com.unbags.ordering.dto.ReviewResponse;
import com.unbags.ordering.mapper.ReviewMapper;
import com.unbags.ordering.service.SnowflakeIdGenerator;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewMapper reviewMapper;
    private final SnowflakeIdGenerator idGenerator;

    public ReviewController(ReviewMapper reviewMapper, SnowflakeIdGenerator idGenerator) {
        this.reviewMapper = reviewMapper;
        this.idGenerator = idGenerator;
    }

    /**
     * 提交订单菜品评价。
     */
    @PostMapping
    public ApiResponse<ReviewResponse> submitReview(@Valid @RequestBody ReviewRequest request) {
        Review review = new Review();
        review.setId(idGenerator.nextId());
        review.setOrderNo(request.getOrderNo());
        review.setDishId(request.getDishId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        reviewMapper.insert(review);
        return ApiResponse.success(toResponse(review));
    }

    /**
     * 根据菜品编号查询该菜品的评价列表。
     */
    @GetMapping("/dish/{dishId}")
    public ApiResponse<List<ReviewResponse>> getDishReviews(@PathVariable Long dishId) {
        List<ReviewResponse> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getDishId, dishId)
                .orderByDesc(Review::getCreatedAt)
        ).stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(reviews);
    }

    /**
     * 根据订单号查询该订单关联的评价列表。
     */
    @GetMapping("/order/{orderNo}")
    public ApiResponse<List<ReviewResponse>> getOrderReviews(@PathVariable String orderNo) {
        List<ReviewResponse> reviews = reviewMapper.selectList(
            new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderNo, orderNo)
                .orderByDesc(Review::getCreatedAt)
        ).stream().map(this::toResponse).collect(Collectors.toList());
        return ApiResponse.success(reviews);
    }

    /**
     * 将评价实体转换为前端响应对象。
     */
    private ReviewResponse toResponse(Review review) {
        ReviewResponse r = new ReviewResponse();
        r.setId(review.getId());
        r.setOrderNo(review.getOrderNo());
        r.setDishId(review.getDishId());
        r.setRating(review.getRating());
        r.setComment(review.getComment());
        r.setCreatedAt(review.getCreatedAt());
        return r;
    }
}
