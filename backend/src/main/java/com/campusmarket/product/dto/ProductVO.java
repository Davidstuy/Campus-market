package com.campusmarket.product.dto;

import com.campusmarket.category.entity.Category;
import com.campusmarket.product.entity.ProductImage;
import com.campusmarket.user.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * ProductVO — 返回给前端的商品视图对象
 *
 * 和 Product 实体的区别：
 * - Product 只有 categoryId（数字），ProductVO 有完整的 Category 对象
 * - Product 只有 sellerId（数字），ProductVO 有完整的 User 对象（卖家信息）
 * - ProductVO 包含 ProductImage 列表
 * - ProductVO 包含 isFavorited（当前用户是否已收藏）
 *
 * 这就是"视图对象"模式：数据库存的是一套，前端看的可能是另一套拼装结果
 */
@Data
public class ProductVO {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private Long categoryId;
    private Long sellerId;
    private String status;
    private String reviewReason;
    private String riskLevel;
    private String coverImage;
    private String contactWechat;
    private String contactQq;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联数据（需要额外查询拼装）
    private User seller;
    private Category category;
    private List<ProductImage> images;
    private Boolean isFavorited;
}
