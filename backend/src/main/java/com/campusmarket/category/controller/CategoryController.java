package com.campusmarket.category.controller;

import com.campusmarket.category.entity.Category;
import com.campusmarket.category.service.CategoryService;
import com.campusmarket.common.response.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 分类接口 — 全部公开访问
 *
 * 这里严格按 REST 规范：GET /api/v1/categories → 返回分类列表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public ApiResult<List<Category>> list() {
        List<Category> categories = categoryService.list();
        return ApiResult.success(categories);
    }
}
