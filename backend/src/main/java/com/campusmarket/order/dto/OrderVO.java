package com.campusmarket.order.dto;

import com.campusmarket.user.entity.User;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String productTitle;
    private BigDecimal productPrice;
    private String productCover;
    private String status;
    private String buyerRemark;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime completedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User buyer;
    private User seller;
}
