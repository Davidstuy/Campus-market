package com.campusmarket.product.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体 — 对应 product 表
 *
 * BigDecimal 用于价格：float/double 有精度问题（0.1 + 0.2 ≠ 0.3）
 * 涉及金额一律用 BigDecimal
 */
@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    @TableField("category_id")
    private Long categoryId;

    @TableField("seller_id")
    private Long sellerId;

    private String status;  // ACTIVE / SOLD / DELISTED

    @TableField("cover_image")
    private String coverImage;

    @TableField("contact_wechat")
    private String contactWechat;

    @TableField("contact_qq")
    private String contactQq;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
