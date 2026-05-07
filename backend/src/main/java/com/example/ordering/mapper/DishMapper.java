package com.example.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ordering.domain.Dish;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface DishMapper extends BaseMapper<Dish> {

    @Update("UPDATE dish "
        + "SET stock = stock - #{quantity}, "
        + "available = CASE WHEN stock - #{quantity} <= 0 THEN 0 ELSE available END "
        + "WHERE id = #{dishId} "
        + "AND deleted = 0 "
        + "AND available = 1 "
        + "AND stock >= 0 "
        + "AND stock >= #{quantity}")
    int decrementStockIfEnough(
        @Param("dishId") Long dishId,
        @Param("quantity") Integer quantity
    );

    @Update("UPDATE dish SET category_id = #{fallbackCategoryId} WHERE category_id = #{categoryId} AND deleted = 1")
    int reassignDeletedDishesCategory(
        @Param("categoryId") String categoryId,
        @Param("fallbackCategoryId") String fallbackCategoryId
    );
}
