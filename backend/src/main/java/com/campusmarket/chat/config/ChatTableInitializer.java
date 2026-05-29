package com.campusmarket.chat.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS chat_conversation (
                    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    buyer_id        BIGINT       NOT NULL COMMENT '买家ID',
                    seller_id       BIGINT       NOT NULL COMMENT '卖家ID',
                    product_id      BIGINT       NOT NULL COMMENT '商品ID',
                    last_message    VARCHAR(500) DEFAULT '' COMMENT '最后一条消息',
                    last_message_at DATETIME     DEFAULT NULL COMMENT '最后消息时间',
                    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY uk_buyer_seller_product (buyer_id, seller_id, product_id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表'
                """);

            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS chat_message (
                    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    conversation_id BIGINT       NOT NULL COMMENT '会话ID',
                    sender_id       BIGINT       NOT NULL COMMENT '发送者',
                    receiver_id     BIGINT       NOT NULL COMMENT '接收者',
                    content         VARCHAR(1000) NOT NULL COMMENT '消息内容',
                    is_read         TINYINT(1)   DEFAULT 0 COMMENT '是否已读',
                    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_conv_created (conversation_id, created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表'
                """);

            log.info("Chat tables ready");
        } catch (Exception e) {
            log.warn("Failed to create chat tables: {}", e.getMessage());
        }
    }
}
