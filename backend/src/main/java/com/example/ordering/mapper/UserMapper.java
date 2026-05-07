package com.example.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ordering.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
