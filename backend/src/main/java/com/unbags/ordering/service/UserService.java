package com.unbags.ordering.service;

import com.unbags.ordering.domain.User;

public interface UserService {

    /** 注册新管理员。若系统已有管理员则抛出异常。返回生成的用户。 */
    User register(String username, String rawPassword, String displayName);

    /** 验证登录凭据，成功返回用户，失败返回 null */
    User authenticate(String username, String rawPassword);

    /** 系统是否已有管理员 */
    boolean hasAdmin();

    /** 根据用户名查找用户 */
    User findByUsername(String username);
}
