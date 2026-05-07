package com.example.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ordering.domain.Review;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {
}
