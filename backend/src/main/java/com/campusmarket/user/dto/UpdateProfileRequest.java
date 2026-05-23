package com.campusmarket.user.dto;

import lombok.Data;

/**
 * 更新个人资料请求 — 前端 PUT /v1/users/me 时传的 JSON body
 */
@Data
public class UpdateProfileRequest {
    private String nickname;
    private String avatarUrl;
    private String phone;
    private String wechat;
    private String qq;
}
