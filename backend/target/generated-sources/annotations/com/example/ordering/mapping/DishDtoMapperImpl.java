package com.example.ordering.mapping;

import com.example.ordering.domain.Dish;
import com.example.ordering.dto.AdminDishResponse;
import com.example.ordering.dto.DishResponse;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T00:36:53+0800",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class DishDtoMapperImpl implements DishDtoMapper {

    @Override
    public DishResponse toDishResponse(Dish dish) {
        if ( dish == null ) {
            return null;
        }

        DishResponse dishResponse = new DishResponse();

        dishResponse.setCategory( dish.getCategoryId() );
        dishResponse.setId( dish.getId() );
        dishResponse.setName( dish.getName() );
        dishResponse.setPrice( dish.getPrice() );
        dishResponse.setRating( dish.getRating() );
        dishResponse.setCalories( dish.getCalories() );
        dishResponse.setDescription( dish.getDescription() );
        dishResponse.setHighlight( dish.getHighlight() );
        dishResponse.setImageUrl( dish.getImageUrl() );

        return dishResponse;
    }

    @Override
    public AdminDishResponse toAdminDishResponse(Dish dish) {
        if ( dish == null ) {
            return null;
        }

        AdminDishResponse adminDishResponse = new AdminDishResponse();

        adminDishResponse.setCategoryId( dish.getCategoryId() );
        adminDishResponse.setAvailable( dish.getAvailable() );
        adminDishResponse.setId( dish.getId() );
        adminDishResponse.setName( dish.getName() );
        adminDishResponse.setPrice( dish.getPrice() );
        adminDishResponse.setRating( dish.getRating() );
        adminDishResponse.setCalories( dish.getCalories() );
        adminDishResponse.setDescription( dish.getDescription() );
        adminDishResponse.setHighlight( dish.getHighlight() );
        adminDishResponse.setImageUrl( dish.getImageUrl() );
        adminDishResponse.setStock( dish.getStock() );

        return adminDishResponse;
    }
}
