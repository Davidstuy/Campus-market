package com.campusmarket.faq.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FaqTableInitializer {

    private final JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        try {
            // ──────── 1. 迁移 chat_conversation 表：添加 type 字段，更新唯一键 ────────
            try {
                jdbcTemplate.execute("""
                    ALTER TABLE chat_conversation
                        ADD COLUMN type VARCHAR(20) NOT NULL DEFAULT 'PRODUCT'
                        COMMENT '会话类型: PRODUCT/SUPPORT'
                        AFTER product_id
                    """);
            } catch (Exception e) {
                log.info("chat_conversation.type column may already exist: {}", e.getMessage());
            }

            try {
                jdbcTemplate.execute(
                    "ALTER TABLE chat_conversation DROP INDEX uk_buyer_seller_product");
            } catch (Exception e) {
                log.info("uk_buyer_seller_product may already be dropped: {}", e.getMessage());
            }

            try {
                jdbcTemplate.execute("""
                    ALTER TABLE chat_conversation
                        ADD UNIQUE KEY uk_buyer_seller_product_type
                        (buyer_id, seller_id, product_id, type)
                    """);
            } catch (Exception e) {
                log.info("uk_buyer_seller_product_type may already exist: {}", e.getMessage());
            }

            // ──────── 2. 创建 faq 表 ────────
            jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS faq (
                    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
                    question   VARCHAR(500) NOT NULL COMMENT '常见问题',
                    answer     TEXT         NOT NULL COMMENT '回答',
                    sort_order INT          DEFAULT 0 COMMENT '排序序号',
                    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
                    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常见问题表'
                """);

            // ──────── 3. 种子数据（仅空表时插入） ────────
            Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM faq", Long.class);
            if (count != null && count == 0) {
                jdbcTemplate.execute("""
                    INSERT INTO faq (question, answer, sort_order) VALUES
                    ('如何发布商品？',
                     '登录后点击导航栏的"发布商品"按钮，填写商品标题、描述、价格等信息并上传图片，提交后等待管理员审核。审核通过后商品将自动上架展示。',
                     1),
                    ('商品审核需要多久？',
                     '我们通常会在24小时内完成审核，请耐心等待。审核结果会通过站内通知及时告知您。如超过24小时仍未审核，请联系客服。',
                     2),
                    ('如何联系卖家？',
                     '在商品详情页点击"联系卖家"按钮，即可与卖家在线沟通。建议在交易前充分了解商品成色、使用时长等信息，确认无误后再下单。',
                     3),
                    ('交易安全吗？如何保障？',
                     '我们强烈建议您选择校园内当面交易，一手交钱一手交货。交易前请仔细核实商品实物，切勿提前支付任何款项。平台仅提供信息展示和沟通渠道，不参与实际交易。',
                     4),
                    ('如何取消订单？',
                     '在"我的订单"页面找到对应订单，点击"取消订单"即可取消。请注意：已发货或已完成的订单无法取消，如有疑问请联系客服处理。',
                     5),
                    ('遇到交易纠纷怎么办？',
                     '请先尝试与对方友好协商解决。如协商无果，可通过页面底部的"联系我们"联系平台客服，我们会协助双方沟通，尽力化解纠纷。',
                     6),
                    ('可以修改已发布的商品吗？',
                     '可以。在"我的发布"中找到对应商品，点击"编辑"即可修改标题、描述、价格、图片等信息。修改后商品状态会重置为"待审核"，需重新通过管理员审核。',
                     7),
                    ('如何下架商品？',
                     '在"我的发布"中找到对应商品，点击"下架"按钮即可立即下架。已售出的商品会自动下架，无需手动操作。',
                     8)
                """);
            }

            log.info("FAQ table and chat_conversation migration ready");
        } catch (Exception e) {
            log.warn("Failed to init FAQ tables: {}", e.getMessage());
        }
    }
}
