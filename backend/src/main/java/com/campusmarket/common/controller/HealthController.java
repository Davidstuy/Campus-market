package com.campusmarket.common.controller;

import com.campusmarket.common.response.ApiResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/v1/health")
    public ApiResult<Map<String, Object>> health() {
        return ApiResult.success(Map.of(
                "status", "UP",
                "time", LocalDateTime.now().toString()
        ));
    }
}
