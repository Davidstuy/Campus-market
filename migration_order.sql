-- 订单表迁移
-- 运行: mysql -u root -p123456 campus_market < migration_order.sql

USE campus_market;

CREATE TABLE IF NOT EXISTS `order_t` (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(32)  NOT NULL UNIQUE COMMENT '订单号 ORD+时间戳+随机',
    buyer_id        BIGINT       NOT NULL COMMENT '买家',
    seller_id       BIGINT       NOT NULL COMMENT '卖家',
    product_id      BIGINT       NOT NULL COMMENT '商品ID',
    product_title   VARCHAR(200) NOT NULL COMMENT '商品快照-标题',
    product_price   DECIMAL(10,2) NOT NULL COMMENT '商品快照-价格',
    product_cover   VARCHAR(500) DEFAULT '' COMMENT '商品快照-封面',
    status          VARCHAR(20)  DEFAULT 'PENDING' COMMENT 'PENDING/PAID/SHIPPED/COMPLETED/CANCELLED',
    buyer_remark    VARCHAR(500) DEFAULT '' COMMENT '买家备注',
    paid_at         DATETIME     DEFAULT NULL COMMENT '支付时间',
    shipped_at      DATETIME     DEFAULT NULL COMMENT '发货时间',
    completed_at    DATETIME     DEFAULT NULL COMMENT '完成时间',
    cancelled_at    DATETIME     DEFAULT NULL COMMENT '取消时间',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_buyer_status (buyer_id, status, created_at),
    INDEX idx_seller_status (seller_id, status, created_at),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';
