package com.campusmarket.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.admin.dto.ReviewRequest;
import com.campusmarket.category.entity.Category;
import com.campusmarket.faq.entity.Faq;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.user.entity.User;

import java.util.List;
import java.util.Map;

/**
 * 管理员服务接口
 */
public interface AdminService {

    /** 数据看板 */
    Map<String, Object> getDashboard();

    /** 待审商品列表（分页） */
    Page<ProductVO> listPendingProducts(int page, int size);

    /** 通过审核 */
    void approveProduct(Long productId);

    /** 驳回商品 */
    void rejectProduct(Long productId, ReviewRequest request);

    /** 用户列表（分页 + 搜索） */
    Page<User> listUsers(int page, int size, String keyword);

    /** 封禁用户 */
    void banUser(Long userId);

    /** 解封用户 */
    void unbanUser(Long userId);

    /** 新增分类 */
    Category createCategory(Category category);

    /** 更新分类 */
    Category updateCategory(Long id, Category category);

    /** 删除分类 */
    void deleteCategory(Long id);

    /** FAQ 列表 */
    List<Faq> listFaqs();

    /** 新增 FAQ */
    Faq createFaq(Faq faq);

    /** 更新 FAQ */
    Faq updateFaq(Long id, Faq faq);

    /** 删除 FAQ */
    void deleteFaq(Long id);
}
