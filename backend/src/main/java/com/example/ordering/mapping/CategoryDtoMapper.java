package com.example.ordering.mapping;

import com.example.ordering.domain.Category;
import com.example.ordering.dto.CategoryResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryDtoMapper {

    CategoryResponse toCategoryResponse(Category category);
}
