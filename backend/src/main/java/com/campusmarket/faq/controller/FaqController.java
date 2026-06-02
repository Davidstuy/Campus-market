package com.campusmarket.faq.controller;

import com.campusmarket.common.response.ApiResult;
import com.campusmarket.faq.entity.Faq;
import com.campusmarket.faq.service.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * FAQ 公开接口 — 无需登录即可访问
 */
@RestController
@RequestMapping("/v1")
@RequiredArgsConstructor
public class FaqController {

    private final FaqService faqService;

    @GetMapping("/faqs")
    public ApiResult<List<Faq>> list() {
        return ApiResult.success(faqService.listAllSorted());
    }
}
