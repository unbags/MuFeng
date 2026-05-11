package com.example.ordering.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.ordering.domain.User;
import com.example.ordering.mapper.UserMapper;
import com.example.ordering.service.SnowflakeIdGenerator;
import com.example.ordering.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SnowflakeIdGenerator idGenerator;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, SnowflakeIdGenerator idGenerator) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.idGenerator = idGenerator;
    }

    /**
     * 注册首个管理员账号，并保存加密后的密码。
     */
    @Override
    @Transactional
    public synchronized User register(String username, String rawPassword, String displayName) {
        if (hasAdmin()) {
            throw new IllegalStateException("系统已初始化，无法重复注册");
        }
        if (findByUsername(username) != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        User user = new User();
        user.setId(idGenerator.nextId());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setDisplayName(displayName);

        userMapper.insert(user);
        return user;
    }

    /**
     * 校验管理员账号密码，认证成功时返回用户信息。
     */
    @Override
    public User authenticate(String username, String rawPassword) {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return null;
        }
        return user;
    }

    /**
     * 判断系统是否已经存在管理员账号。
     */
    @Override
    public boolean hasAdmin() {
        return userMapper.selectCount(null) > 0;
    }

    /**
     * 根据用户名查询管理员用户。
     */
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }
}
