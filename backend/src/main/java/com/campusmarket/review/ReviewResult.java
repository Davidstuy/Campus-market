package com.campusmarket.review;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 审核结果
 *
 * @param riskLevel 风险等级: LOW / HIGH
 * @param reason    风险原因（HIGH 时说明触发了哪条规则）
 */
@Data
@AllArgsConstructor
public class ReviewResult {
    private String riskLevel;  // LOW / HIGH
    private String reason;
}
