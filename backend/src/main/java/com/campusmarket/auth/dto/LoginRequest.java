package com.campusmarket.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求 DTO
 * @NotBlank — Spring Validation 校验，确保字段不为 null 且不是空字符串
 * 如果校验失败，会抛出 MethodArgumentNotValidException
 * → 被 GlobalExceptionHandler 捕获 → 返回 400 + 具体错误信息
 */
@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
