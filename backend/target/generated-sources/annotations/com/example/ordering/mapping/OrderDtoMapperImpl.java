package com.example.ordering.mapping;

import com.example.ordering.domain.CustomerOrder;
import com.example.ordering.domain.OrderItem;
import com.example.ordering.dto.OrderDetailItemResponse;
import com.example.ordering.dto.OrderSummaryResponse;
import javax.annotation.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-08T00:36:53+0800",
    comments = "version: 1.5.5.Final, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class OrderDtoMapperImpl implements OrderDtoMapper {

    @Override
    public OrderSummaryResponse toOrderSummary(CustomerOrder order) {
        if ( order == null ) {
            return null;
        }

        OrderSummaryResponse orderSummaryResponse = new OrderSummaryResponse();

        orderSummaryResponse.setOrderNo( order.getOrderNo() );
        orderSummaryResponse.setOrderType( order.getOrderType() );
        orderSummaryResponse.setStatus( order.getStatus() );
        orderSummaryResponse.setItemCount( order.getItemCount() );
        orderSummaryResponse.setTotalAmount( order.getTotalAmount() );
        orderSummaryResponse.setCreatedAt( order.getCreatedAt() );

        return orderSummaryResponse;
    }

    @Override
    public OrderDetailItemResponse toOrderDetailItem(OrderItem item) {
        if ( item == null ) {
            return null;
        }

        OrderDetailItemResponse orderDetailItemResponse = new OrderDetailItemResponse();

        orderDetailItemResponse.setName( item.getDishName() );
        orderDetailItemResponse.setPrice( item.getDishPrice() );
        orderDetailItemResponse.setTotal( item.getLineTotal() );
        orderDetailItemResponse.setDishId( item.getDishId() );
        orderDetailItemResponse.setQuantity( item.getQuantity() );

        return orderDetailItemResponse;
    }
}
