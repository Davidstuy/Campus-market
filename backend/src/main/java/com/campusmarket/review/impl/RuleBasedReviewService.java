package com.campusmarket.review.impl;

import com.campusmarket.product.dto.CreateProductRequest;
import com.campusmarket.review.ContentReviewService;
import com.campusmarket.review.ReviewResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 规则引擎审核实现
 *
 * 四层规则，任一命中即判定 HIGH 风险：
 * 1. 违禁词库
 * 2. 联系方式异常（title 中含手机号/微信号）
 * 3. 价格异常（0 元或 >10 万元）
 * 4. 内容过短（title < 2 字 或 description < 5 字）
 *
 * 通过 campus-market.review.engine=rule 启用（默认）
 */
@Component
@ConditionalOnProperty(name = "campus-market.review.engine", havingValue = "rule", matchIfMissing = true)
public class RuleBasedReviewService implements ContentReviewService {

    // ──────────── 违禁词库 ────────────
    private static final List<String> BANNED_KEYWORDS = List.of(
            "毒品", "大麻", "海洛因", "冰毒", "摇头丸", "k粉",
            "枪支", "手枪", "步枪", "子弹", "炸药", "雷管",
            "假币", "假钞", "伪钞", "假钱",
            "发票", "代开发票", "增值税发票",
            "赌博", "赌场", "赌球", "六合彩", "百家乐",
            "色情", "淫秽", "裸聊", "约炮", "嫖娼",
            "传销", "直销", "拉人头",
            "高利贷", "裸贷", "校园贷",
            "作弊", "代考", "替考", "四六级答案"
    );

    // ──────────── 正则 ────────────
    // 手机号：1 开头 10 位数字
    private static final Pattern PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    // 微信号：wx_ 或 weixin 或 vx: 或 微信 开头，后跟字母数字
    private static final Pattern WECHAT_PATTERN = Pattern.compile(
            "(微信|vx|wechat|weixin)\\s*[:：]?\\s*[a-zA-Z0-9_-]+",
            Pattern.CASE_INSENSITIVE
    );

    // ──────────── 阈值 ────────────
    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("100000");

    @Override
    public ReviewResult review(CreateProductRequest product) {
        String title = product.getTitle() != null ? product.getTitle().trim() : "";
        String desc = product.getDescription() != null ? product.getDescription().trim() : "";
        BigDecimal price = product.getPrice();

        // 规则 1：违禁词检查
        String bannedHit = checkBannedKeywords(title, desc);
        if (bannedHit != null) {
            return new ReviewResult("HIGH", "内容包含违禁词：" + bannedHit);
        }

        // 规则 2：联系方式异常（只在 title 中检测，description 中的联系方式是合理的）
        if (PHONE_PATTERN.matcher(title).find()) {
            return new ReviewResult("HIGH", "标题中包含手机号码，疑似引流到站外交易");
        }
        if (WECHAT_PATTERN.matcher(title).find()) {
            return new ReviewResult("HIGH", "标题中包含微信号，疑似引流到站外交易");
        }

        // 规则 3：价格异常
        if (price == null || price.compareTo(MIN_PRICE) < 0) {
            return new ReviewResult("HIGH", "价格异常：价格不能为 0 或负数");
        }
        if (price.compareTo(MAX_PRICE) > 0) {
            return new ReviewResult("HIGH", "价格异常：商品价格超过 10 万元上限");
        }

        // 规则 4：内容过短
        if (title.length() < 2) {
            return new ReviewResult("HIGH", "标题过短（少于 2 个字符），疑似无效发布");
        }
        if (desc.length() < 5) {
            return new ReviewResult("HIGH", "描述过短（少于 5 个字符），请补充商品详情");
        }

        // 全部通过 → 低风险
        return new ReviewResult("LOW", null);
    }

    /**
     * 检查标题和描述中是否包含违禁词
     * @return 命中的第一个违禁词，未命中返回 null
     */
    private String checkBannedKeywords(String title, String description) {
        String combined = (title + " " + description).toLowerCase();
        for (String keyword : BANNED_KEYWORDS) {
            if (combined.contains(keyword.toLowerCase())) {
                return keyword;
            }
        }
        return null;
    }
}
