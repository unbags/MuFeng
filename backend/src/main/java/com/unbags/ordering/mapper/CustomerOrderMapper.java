package com.unbags.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unbags.ordering.domain.CustomerOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface CustomerOrderMapper extends BaseMapper<CustomerOrder> {

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM customer_order WHERE status <> 'CANCELLED'")
    BigDecimal sumTotalRevenue();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM customer_order WHERE created_at >= #{todayStart} AND status <> 'CANCELLED'")
    BigDecimal sumTodayRevenue(@Param("todayStart") LocalDateTime todayStart);

    @Select("SELECT COUNT(*) FROM customer_order WHERE created_at >= #{todayStart} AND status <> 'CANCELLED'")
    Integer countTodayOrders(@Param("todayStart") LocalDateTime todayStart);
}
