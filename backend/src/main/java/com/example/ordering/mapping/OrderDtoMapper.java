package com.example.ordering.mapping;

import com.example.ordering.domain.CustomerOrder;
import com.example.ordering.domain.OrderItem;
import com.example.ordering.dto.OrderDetailItemResponse;
import com.example.ordering.dto.OrderSummaryResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderDtoMapper {

    OrderSummaryResponse toOrderSummary(CustomerOrder order);

    @Mapping(target = "name", source = "dishName")
    @Mapping(target = "price", source = "dishPrice")
    @Mapping(target = "total", source = "lineTotal")
    @Mapping(target = "imageUrl", ignore = true)
    OrderDetailItemResponse toOrderDetailItem(OrderItem item);
}
