package com.campusmarket.product.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.common.response.PageResult;
import com.campusmarket.product.dto.CreateProductRequest;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.product.service.ProductService;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商品接口
 *
 * RESTful 风格：
 * GET    /v1/products          → 商品列表（公开）
 * GET    /v1/products/{id}     → 商品详情（公开）
 * POST   /v1/products          → 发布商品（需登录，Phase 4）
 * PUT    /v1/products/{id}     → 编辑商品（需登录，Phase 5）
 * DELETE /v1/products/{id}     → 删除商品（需登录，Phase 5）
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/products")
public class ProductController {

    private final ProductService productService;

    /**
     * 商品列表 — 分页 + 筛选 + 搜索 + 排序
     *
     * @RequestParam(defaultValue = "...") — 参数可选，有默认值
     */
    @GetMapping
    public ApiResult<PageResult<ProductVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long sellerId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort) {

        Page<ProductVO> result = productService.listProducts(page, size, categoryId, sellerId, keyword, sort);

        return ApiResult.success(PageResult.of(
                result.getRecords(),
                result.getTotal(),
                result.getCurrent(),
                result.getSize()
        ));
    }

    /**
     * 商品详情
     *
     * @PathVariable — 从 URL 路径中提取参数（/v1/products/123 → id = 123）
     */
    @GetMapping("/{id}")
    public ApiResult<ProductVO> detail(@PathVariable Long id) {
        ProductVO vo = productService.getProductDetail(id);
        return ApiResult.success(vo);
    }

    /**
     * 发布商品
     *
     * @Valid：触发 JSR-303 Bean Validation（校验 CreateProductRequest 中的注解）
     * 校验失败时 Spring 自动返回 400，不会进入方法体
     *
     * 当前登录用户从 JwtAuthenticationFilter 设置的 request attribute 中获取
     */
    @PostMapping
    public ApiResult<ProductVO> create(@Valid @RequestBody CreateProductRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ProductVO vo = productService.createProduct(request, userId);
        return ApiResult.success(vo);
    }

    /**
     * 我的发布列表 — 需登录
     *
     * 和公开列表的区别：
     * - 只查当前用户的商品
     * - 包含所有状态（在售/已售/已下架），不只是 ACTIVE
     * - 不包含卖家信息（因为肯定是自己）
     */
    @GetMapping("/mine")
    public ApiResult<PageResult<ProductVO>> mine(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        Page<ProductVO> result = productService.listMyProducts(page, size, userId);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /**
     * 编辑商品 — 需登录、只能编辑自己的
     */
    @PutMapping("/{id}")
    public ApiResult<ProductVO> update(@PathVariable Long id,
                                       @Valid @RequestBody CreateProductRequest request,
                                       HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        ProductVO vo = productService.updateProduct(id, request, userId);
        return ApiResult.success(vo);
    }

    /**
     * 删除商品 — 需登录、只能删除自己的
     */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id,
                                  HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        productService.deleteProduct(id, userId);
        return ApiResult.success(null);
    }

    /**
     * 更新商品状态 — 需登录、只能操作自己的
     * 请求体：{ "status": "SOLD" } 或 { "status": "DELISTED" }
     */
    @PutMapping("/{id}/status")
    public ApiResult<Void> updateStatus(@PathVariable Long id,
                                        @RequestBody Map<String, String> body,
                                        HttpServletRequest httpRequest) {
        Long userId = getCurrentUserId(httpRequest);
        String status = body.get("status");
        productService.updateProductStatus(id, status, userId);
        return ApiResult.success(null);
    }

    /**
     * 从 request attribute 获取当前登录用户 ID
     * currentUser 是 JwtAuthenticationFilter 在认证成功后存入的
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user.getId();
    }
}
