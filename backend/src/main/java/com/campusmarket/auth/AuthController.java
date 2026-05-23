package com.campusmarket.auth;

import com.campusmarket.auth.dto.LoginRequest;
import com.campusmarket.auth.dto.LoginResponse;
import com.campusmarket.auth.dto.RegisterRequest;
import com.campusmarket.common.response.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证接口 — 注册和登录
 *
 * 路由设计：POST /api/v1/auth/register 和 POST /api/v1/auth/login
 * 注意 application.yml 中 servlet.context-path=/api，所以这里只需写 /v1/auth/xxx
 *
 * @Valid — 触发 DTO 上的校验注解（@NotBlank, @Size 等）
 *         校验失败 → MethodArgumentNotValidException → GlobalExceptionHandler 统一处理
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResult<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResult.success();
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResult.success(response);
    }
}
