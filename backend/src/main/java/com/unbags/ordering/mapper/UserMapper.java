package com.unbags.ordering.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.unbags.ordering.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
