package com.campusmarket.common.exception;

import com.campusmarket.common.response.ApiResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 所有 Controller 抛出的异常统一在这里拦截，转成 ApiResult 格式
 * 这样 Controller 里就不需要写 try-catch 了
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理参数校验失败（@Valid 注解触发的）
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResult<Void> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return ApiResult.error(400, msg);
    }

    // 处理业务异常
    @ExceptionHandler(BusinessException.class)
    public ApiResult<Void> handleBusiness(BusinessException e) {
        return ApiResult.error(e.getCode(), e.getMessage());
    }

    // 处理其他未预料的异常
    @ExceptionHandler(Exception.class)
    public ApiResult<Void> handleUnknown(Exception e) {
        log.error("未知异常", e);
        return ApiResult.error(500, "服务器内部错误");
    }
}
