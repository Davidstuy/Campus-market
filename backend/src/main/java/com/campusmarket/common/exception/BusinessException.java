package com.campusmarket.common.exception;

import lombok.Getter;

/**
 * 业务异常 — 不是系统崩溃，是规则上不允许（如"用户名已存在"）
 * 会被 GlobalExceptionHandler 捕获，转成友好的错误响应
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400, message);
    }
}
