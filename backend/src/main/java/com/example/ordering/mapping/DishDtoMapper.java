package com.example.ordering.mapping;

import com.example.ordering.domain.Dish;
import com.example.ordering.dto.AdminDishResponse;
import com.example.ordering.dto.DishResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DishDtoMapper {

    @Mapping(target = "category", source = "categoryId")
    DishResponse toDishResponse(Dish dish);

    @Mapping(target = "categoryId", source = "categoryId")
    @Mapping(target = "categoryLabel", ignore = true)
    @Mapping(target = "available", source = "available")
    AdminDishResponse toAdminDishResponse(Dish dish);
}
