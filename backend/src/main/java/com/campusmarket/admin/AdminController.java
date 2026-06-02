package com.campusmarket.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.admin.dto.ReviewRequest;
import com.campusmarket.category.entity.Category;
import com.campusmarket.faq.entity.Faq;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.user.entity.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 *
 * 所有接口路径以 /v1/admin/ 开头，JwtAuthenticationFilter 中已校验 ADMIN 角色
 */
@RestController
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /** 数据看板 */
    @GetMapping("/dashboard")
    public ApiResult<Map<String, Object>> dashboard() {
        return ApiResult.success(adminService.getDashboard());
    }

    /** 待审商品列表 */
    @GetMapping("/products/pending")
    public ApiResult<Page<ProductVO>> listPending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResult.success(adminService.listPendingProducts(page, size));
    }

    /** 通过审核 */
    @PutMapping("/products/{id}/approve")
    public ApiResult<Void> approve(@PathVariable Long id) {
        adminService.approveProduct(id);
        return ApiResult.success();
    }

    /** 驳回商品 */
    @PutMapping("/products/{id}/reject")
    public ApiResult<Void> reject(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        adminService.rejectProduct(id, request);
        return ApiResult.success();
    }

    /** 用户列表 */
    @GetMapping("/users")
    public ApiResult<Page<User>> listUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        return ApiResult.success(adminService.listUsers(page, size, keyword));
    }

    /** 封禁用户 */
    @PutMapping("/users/{id}/ban")
    public ApiResult<Void> banUser(@PathVariable Long id) {
        adminService.banUser(id);
        return ApiResult.success();
    }

    /** 解封用户 */
    @PutMapping("/users/{id}/unban")
    public ApiResult<Void> unbanUser(@PathVariable Long id) {
        adminService.unbanUser(id);
        return ApiResult.success();
    }

    /** 新增分类 */
    @PostMapping("/categories")
    public ApiResult<Category> createCategory(@RequestBody Category category) {
        return ApiResult.success(adminService.createCategory(category));
    }

    /** 编辑分类 */
    @PutMapping("/categories/{id}")
    public ApiResult<Category> updateCategory(@PathVariable Long id, @RequestBody Category category) {
        return ApiResult.success(adminService.updateCategory(id, category));
    }

    /** 删除分类 */
    @DeleteMapping("/categories/{id}")
    public ApiResult<Void> deleteCategory(@PathVariable Long id) {
        adminService.deleteCategory(id);
        return ApiResult.success();
    }

    // ──────── FAQ 管理 ────────

    /** FAQ 列表 */
    @GetMapping("/faqs")
    public ApiResult<List<Faq>> listFaqs() {
        return ApiResult.success(adminService.listFaqs());
    }

    /** 新增 FAQ */
    @PostMapping("/faqs")
    public ApiResult<Faq> createFaq(@Valid @RequestBody Faq faq) {
        return ApiResult.success(adminService.createFaq(faq));
    }

    /** 编辑 FAQ */
    @PutMapping("/faqs/{id}")
    public ApiResult<Faq> updateFaq(@PathVariable Long id, @Valid @RequestBody Faq faq) {
        return ApiResult.success(adminService.updateFaq(id, faq));
    }

    /** 删除 FAQ */
    @DeleteMapping("/faqs/{id}")
    public ApiResult<Void> deleteFaq(@PathVariable Long id) {
        adminService.deleteFaq(id);
        return ApiResult.success();
    }
}
