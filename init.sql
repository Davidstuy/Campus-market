-- =============================================
-- 校园二手交易市场 — 数据库初始化脚本
-- 运行方式: mysql -u root -p < init.sql
-- =============================================

CREATE DATABASE IF NOT EXISTS campus_market
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE campus_market;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名，登录用',
    password    VARCHAR(255) NOT NULL        COMMENT 'bcrypt 加密后的密码',
    nickname    VARCHAR(50)  DEFAULT ''      COMMENT '昵称',
    avatar_url  VARCHAR(500) DEFAULT ''      COMMENT '头像 URL',
    phone       VARCHAR(20)  DEFAULT ''      COMMENT '手机号',
    wechat      VARCHAR(50)  DEFAULT ''      COMMENT '微信号',
    qq          VARCHAR(20)  DEFAULT ''      COMMENT 'QQ 号',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 分类表
CREATE TABLE IF NOT EXISTS `category` (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL COMMENT '分类名称',
    icon       VARCHAR(50) DEFAULT ''  COMMENT '图标（Element Plus icon 名称）',
    sort_order INT         DEFAULT 0   COMMENT '排序序号',
    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 分类种子数据
INSERT IGNORE INTO `category` (name, icon, sort_order) VALUES
    ('书籍',    'Reading',  1),
    ('电子产品', 'Cellphone', 2),
    ('服装',    'Shirt',     3),
    ('运动',    'Football',  4),
    ('日用品',  'Box',       5),
    ('出行',    'Bicycle',   6),
    ('辅导',    'Notebook', 7),
    ('其他',    'More',     8);

-- 商品表
CREATE TABLE IF NOT EXISTS `product` (
    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
    title           VARCHAR(200) NOT NULL        COMMENT '商品标题',
    description     TEXT                          COMMENT '商品描述',
    price           DECIMAL(10,2) NOT NULL       COMMENT '价格',
    category_id     BIGINT       NOT NULL        COMMENT '所属分类',
    seller_id       BIGINT       NOT NULL        COMMENT '发布者',
    status          VARCHAR(20)  DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/SOLD/DELISTED',
    cover_image     VARCHAR(500) DEFAULT ''      COMMENT '封面图 URL',
    contact_wechat  VARCHAR(50)  DEFAULT ''      COMMENT '联系方式-微信',
    contact_qq      VARCHAR(20)  DEFAULT ''      COMMENT '联系方式-QQ',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status_created (status, created_at),
    INDEX idx_status_cat_created (status, category_id, created_at),
    INDEX idx_seller_created (seller_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品图片表
CREATE TABLE IF NOT EXISTS `product_image` (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT       NOT NULL COMMENT '所属商品',
    url        VARCHAR(500) NOT NULL COMMENT '图片 URL',
    sort_order INT          DEFAULT 0 COMMENT '排序序号',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_sort (product_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

-- 订单表
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

-- 收藏表
CREATE TABLE IF NOT EXISTS `favorite` (
    id         BIGINT   AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT   NOT NULL COMMENT '用户',
    product_id BIGINT   NOT NULL COMMENT '商品',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user (user_id),
    INDEX idx_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏表';
