package com.campusmarket.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应格式
 * 前端收到的一律是 { code, message, data }，不需要猜测返回结构
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    // 快捷成功（无数据）
    public static <T> ApiResult<T> success() {
        return new ApiResult<>(200, "success", null);
    }

    // 快捷成功（带数据）
    public static <T> ApiResult<T> success(T data) {
        return new ApiResult<>(200, "success", data);
    }

    // 快捷失败
    public static <T> ApiResult<T> error(int code, String message) {
        return new ApiResult<>(code, message, null);
    }
}
