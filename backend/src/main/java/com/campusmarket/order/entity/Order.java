package com.campusmarket.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_t")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_no")
    private String orderNo;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("product_id")
    private Long productId;

    @TableField("product_title")
    private String productTitle;

    @TableField("product_price")
    private BigDecimal productPrice;

    @TableField("product_cover")
    private String productCover;

    private String status;  // PENDING / PAID / SHIPPED / COMPLETED / CANCELLED

    @TableField("buyer_remark")
    private String buyerRemark;

    @TableField("paid_at")
    private LocalDateTime paidAt;

    @TableField("shipped_at")
    private LocalDateTime shippedAt;

    @TableField("completed_at")
    private LocalDateTime completedAt;

    @TableField("cancelled_at")
    private LocalDateTime cancelledAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
