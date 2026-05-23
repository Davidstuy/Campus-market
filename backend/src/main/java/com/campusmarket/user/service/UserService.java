package com.campusmarket.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.user.entity.User;

/**
 * IService<T> 是 MyBatis-Plus 提供的 Service 层接口，封装了常用的业务方法：
 * - save(entity) / saveBatch(list)   保存
 * - getById(id)                      按主键查
 * - list() / list(wrapper)           列表查询
 * - page(page, wrapper)              分页查询
 * - updateById(entity)               按主键更新
 * - removeById(id)                   按主键删除
 */
public interface UserService extends IService<User> {

    /**
     * 检查用户名是否已被注册
     * MyBatis-Plus 的 QueryWrapper 可以构建 where 条件
     */
    boolean existsByUsername(String username);
}
