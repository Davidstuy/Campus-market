package com.campusmarket.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campusmarket.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 继承 BaseMapper<User> 后，自动拥有以下方法（无需写一行 SQL）：
 * - insert(user)          插入用户
 * - selectById(id)        按主键查询
 * - selectList(wrapper)   条件查询列表
 * - selectPage(page, ...) 分页查询
 * - updateById(user)      按主键更新
 * - deleteById(id)        按主键删除
 *
 * 如果这些方法不够用，可以在这里定义自定义方法 + 写 XML 或注解 SQL
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 目前不需要自定义方法，BaseMapper 提供的方法已经够用
}
