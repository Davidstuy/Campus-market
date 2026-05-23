package com.campusmarket.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 创建商品请求 DTO
 *
 * @NotNull vs @NotBlank 区别：
 * - @NotNull：不能为 null
 * - @NotBlank：不能为 null、不能为空串、不能全是空格（用于字符串）
 * - @NotEmpty：不能为 null、不能为空串（用于集合/字符串）
 *
 * @Valid 在 Controller 中触发校验，Spring 会自动返回 400 错误
 */
@Data
public class CreateProductRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotNull(message = "价格不能为空")
    @Positive(message = "价格必须大于 0")
    private BigDecimal price;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private String description;

    private String coverImage;

    // 多图 URL 列表（上传完成后前端传入）
    private List<String> images;

    private String contactWechat;

    private String contactQq;
}
