package com.campusmarket.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import com.campusmarket.user.service.UserService;
import org.springframework.stereotype.Service;

/**
 * ServiceImpl<M, T> 是 IService<T> 的默认实现：
 * - M = Mapper 类型 (UserMapper)
 * - T = Entity 类型 (User)
 *
 * @Service 告诉 Spring：这是一个 Service Bean，由 Spring 管理生命周期
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * QueryWrapper 是 MyBatis-Plus 的条件构造器，用 Java 代码代替 SQL 的 WHERE：
     * eq("username", username) → WHERE username = ?
     * count(wrapper) → SELECT COUNT(*) FROM user WHERE ...
     */
    @Override
    public boolean existsByUsername(String username) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("username", username);
        return this.count(wrapper) > 0;
    }
}
