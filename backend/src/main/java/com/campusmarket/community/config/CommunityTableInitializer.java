package com.campusmarket.community.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            // 社区主题表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_topic (
                    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
                    name       VARCHAR(50) NOT NULL COMMENT '主题名称',
                    icon       VARCHAR(50) DEFAULT '' COMMENT '图标（Element Plus icon 名称）',
                    sort_order INT         DEFAULT 0 COMMENT '排序序号',
                    created_at DATETIME    DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区主题表'
                """);

            // 种子数据（仅在空表时插入，避免重复）
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM community_topic", Long.class);
            if (count != null && count == 0) {
                jdbcTemplate.execute("""
                    INSERT INTO community_topic (name, icon, sort_order) VALUES
                        ('二次元',   'MagicStick',  1),
                    ('穿搭',    'Shirt',       2),
                    ('数码',    'Cellphone',    3),
                    ('美食',    'DishDot',      4),
                    ('校园生活', 'School',      5),
                    ('求助',    'QuestionFilled', 6),
                    ('闲置交换', 'Present',     7),
                    ('游戏',    'VideoGame',    8),
                    ('运动',    'Football',     9),
                    ('其他',    'MoreFilled',   10)
                """);
            }

            // 社区帖子表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_post (
                    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    user_id    BIGINT       NOT NULL COMMENT '作者ID',
                    topic_id   BIGINT       NOT NULL COMMENT '主题ID',
                    title      VARCHAR(200) NOT NULL COMMENT '标题',
                    content    TEXT         NOT NULL COMMENT '正文（支持emoji）',
                    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                    INDEX idx_user_created (user_id, created_at),
                    INDEX idx_topic_created (topic_id, created_at),
                    INDEX idx_created (created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表'
                """);

            // 社区帖子媒体表（图片/视频）
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_post_media (
                    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    post_id    BIGINT       NOT NULL COMMENT '帖子ID',
                    media_type VARCHAR(10)  NOT NULL DEFAULT 'IMAGE' COMMENT '媒体类型：IMAGE/VIDEO',
                    url        VARCHAR(500) NOT NULL COMMENT '媒体URL',
                    sort_order INT          DEFAULT 0 COMMENT '排序',
                    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_post_sort (post_id, sort_order)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='帖子媒体表'
                """);

            // 社区评论表
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS community_comment (
                    id              BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    post_id         BIGINT       NOT NULL COMMENT '帖子ID',
                    user_id         BIGINT       NOT NULL COMMENT '评论者',
                    content         TEXT         NOT NULL COMMENT '评论内容（支持emoji）',
                    image_url       VARCHAR(500) DEFAULT NULL COMMENT '可选图片URL',
                    video_url       VARCHAR(500) DEFAULT NULL COMMENT '可选视频URL',
                    parent_id       BIGINT       DEFAULT NULL COMMENT '父评论ID',
                    reply_to_user_id BIGINT      DEFAULT NULL COMMENT '被回复用户ID',
                    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    INDEX idx_post_created (post_id, created_at)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区评论表'
                """);

            log.info("Community tables ready");
        } catch (Exception e) {
            log.warn("Failed to create community tables: {}", e.getMessage());
        }
    }
}
