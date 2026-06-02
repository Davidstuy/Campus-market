package com.campusmarket.review;

import com.campusmarket.product.dto.CreateProductRequest;

/**
 * 内容审核服务接口（策略模式）
 *
 * 当前用规则引擎实现，后续切换到 AI 审核只需新增一个实现类，
 * 通过配置 campus-market.review.engine=ai 即可切换。
 */
public interface ContentReviewService {

    /**
     * 审核商品发布内容
     *
     * @param product 商品发布请求
     * @return 审核结果（风险等级 + 原因）
     */
    ReviewResult review(CreateProductRequest product);
}
