package com.campusmarket.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 — 和数据库 user 表一对一映射
 *
 * 核心注解解释：
 * @TableName("user")   — 告诉 MyBatis-Plus 这个类对应哪张表
 * @TableId(type=IdType.AUTO) — 主键，数据库自增
 * @TableField("xxx")   — 当 Java 字段名和数据库列名不一致时使用（如 avatarUrl ↔ avatar_url）
 * @Data — Lombok 注解，编译时自动生成 getter/setter/toString/equals/hashCode
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;   // bcrypt 加密后的密文，不是明文！

    private String nickname;

    @TableField("avatar_url")
    private String avatarUrl;

    private String phone;

    private String wechat;

    private String qq;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
