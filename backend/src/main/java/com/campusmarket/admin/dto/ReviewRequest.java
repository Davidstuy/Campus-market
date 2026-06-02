package com.campusmarket.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 管理员审核请求
 */
@Data
public class ReviewRequest {
    @NotBlank(message = "驳回时必须填写原因")
    private String reason;
}
