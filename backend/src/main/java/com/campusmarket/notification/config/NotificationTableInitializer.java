package com.campusmarket.notification.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS notification (
                    id           BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    recipient_id BIGINT       NOT NULL COMMENT '接收者用户ID',
                    type         VARCHAR(32)  NOT NULL COMMENT '通知类型',
                    title        VARCHAR(100) NOT NULL COMMENT '通知标题',
                    content      VARCHAR(500) NOT NULL COMMENT '通知内容',
                    order_id     BIGINT       DEFAULT NULL COMMENT '关联订单ID',
                    is_read      TINYINT(1)   DEFAULT 0 COMMENT '是否已读',
                    created_at   DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_recipient (recipient_id, is_read, created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表'
                """);
            log.info("Notification table ready");
        } catch (Exception e) {
            log.warn("Failed to create notification table: {}", e.getMessage());
        }
    }
}
