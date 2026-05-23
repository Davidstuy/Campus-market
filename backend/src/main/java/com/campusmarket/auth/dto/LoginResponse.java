package com.campusmarket.auth.dto;

import com.campusmarket.user.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 登录响应 — 返回 token + 用户信息（不含密码）
 */
@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private User user;
}
