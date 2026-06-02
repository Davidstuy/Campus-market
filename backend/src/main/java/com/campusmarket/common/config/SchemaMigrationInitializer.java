package com.campusmarket.common.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库 Schema 迁移（ALTER TABLE 对已有表添加新列）
 * 每个迁移用 try/catch 包裹，列已存在则跳过
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SchemaMigrationInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        migrateChatMessage();
        migrateProductComment();
        migrateCommunityTopic();
        migrateUserPhoneUnique();
        cleanChatNotifications();
        log.info("Schema migration completed");
    }

    private void migrateChatMessage() {
        // message_type: TEXT / IMAGE / VIDEO
        safeExecute("ALTER TABLE chat_message ADD COLUMN message_type VARCHAR(10) NOT NULL DEFAULT 'TEXT' COMMENT '消息类型：TEXT/IMAGE/VIDEO'");
        // image_url: 图片消息的图片 URL
        safeExecute("ALTER TABLE chat_message ADD COLUMN image_url VARCHAR(500) DEFAULT NULL COMMENT '图片URL（IMAGE类型）'");
        // video_url: 视频消息的视频 URL
        safeExecute("ALTER TABLE chat_message ADD COLUMN video_url VARCHAR(500) DEFAULT NULL COMMENT '视频URL（VIDEO类型）'");
        // 扩大 content 列，支持更长的文本 + emoji
        safeExecute("ALTER TABLE chat_message MODIFY COLUMN content VARCHAR(2000) DEFAULT '' COMMENT '消息文本（支持emoji）'");
    }

    private void migrateProductComment() {
        // image_url: 评论可附带一张图片
        safeExecute("ALTER TABLE product_comment ADD COLUMN image_url VARCHAR(500) DEFAULT NULL COMMENT '可选图片URL'");
        // video_url: 评论可附带一个视频
        safeExecute("ALTER TABLE product_comment ADD COLUMN video_url VARCHAR(500) DEFAULT NULL COMMENT '可选视频URL'");
        // 扩大 content 列
        safeExecute("ALTER TABLE product_comment MODIFY COLUMN content VARCHAR(2000) NOT NULL COMMENT '评论内容（支持emoji）'");
    }

    private void migrateCommunityTopic() {
        // 删除重复的种子数据（保留 id 最小的那一批）
        try {
            jdbcTemplate.execute("""
                DELETE t1 FROM community_topic t1
                INNER JOIN community_topic t2
                ON t1.name = t2.name AND t1.id > t2.id
                """);
            log.debug("Cleaned duplicate community topics");
        } catch (Exception e) {
            log.debug("Skip topic cleanup: {}", e.getMessage().split("\n")[0]);
        }
        // 添加唯一约束，防止以后再插入重复主题
        safeExecute("ALTER TABLE community_topic ADD UNIQUE INDEX uk_name (name)");
    }

    private void cleanChatNotifications() {
        try {
            int deleted = jdbcTemplate.update("DELETE FROM notification WHERE type = 'CHAT'");
            if (deleted > 0) {
                log.info("Cleaned {} CHAT notifications", deleted);
            }
        } catch (Exception e) {
            log.debug("Skip CHAT notification cleanup: {}", e.getMessage().split("\n")[0]);
        }
    }

    private void migrateUserPhoneUnique() {
        // 先把空手机号设为 NULL，否则 UNIQUE 约束会冲突
        try {
            jdbcTemplate.execute("UPDATE user SET phone = NULL WHERE phone = ''");
        } catch (Exception e) {
            log.debug("Skip phone nullify: {}", e.getMessage().split("\n")[0]);
        }
        // 添加手机号唯一索引（NULL 值不冲突）
        safeExecute("CREATE UNIQUE INDEX uk_phone ON user(phone)");
    }

    private void safeExecute(String sql) {
        try {
            jdbcTemplate.execute(sql);
            log.debug("Executed: {}", sql.substring(0, Math.min(60, sql.length())));
        } catch (Exception e) {
            log.debug("Skip (already applied): {}", e.getMessage().split("\n")[0]);
        }
    }
}
