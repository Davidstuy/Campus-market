package com.campusmarket.admin.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.admin.AdminService;
import com.campusmarket.admin.dto.ReviewRequest;
import com.campusmarket.category.entity.Category;
import com.campusmarket.category.mapper.CategoryMapper;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.faq.entity.Faq;
import com.campusmarket.faq.mapper.FaqMapper;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.product.entity.Product;
import com.campusmarket.product.entity.ProductImage;
import com.campusmarket.product.mapper.ProductImageMapper;
import com.campusmarket.product.mapper.ProductMapper;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<ProductMapper, Product> implements AdminService {

    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;
    private final FaqMapper faqMapper;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalUsers", userMapper.selectCount(null));

        stats.put("totalProducts", productMapper.selectCount(null));

        long activeProducts = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "ACTIVE"));
        stats.put("activeProducts", activeProducts);

        long pendingCount = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getStatus, "PENDING_REVIEW"));
        stats.put("pendingReview", pendingCount);

        stats.put("totalCategories", categoryMapper.selectCount(null));

        return stats;
    }

    @Override
    public Page<ProductVO> listPendingProducts(int pageNum, int size) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, "PENDING_REVIEW");
        wrapper.orderByAsc(Product::getCreatedAt);  // 早发布的先审

        long total = this.count(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<Product> records = this.list(wrapper);

        if (records.isEmpty()) {
            Page<ProductVO> empty = new Page<>(pageNum, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        List<ProductVO> voList = toVOList(records);

        Page<ProductVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    @Transactional
    public void approveProduct(Long productId) {
        Product product = getProductOrThrow(productId);
        if (!"PENDING_REVIEW".equals(product.getStatus())) {
            throw new BusinessException("该商品不在待审核状态");
        }
        product.setStatus("ACTIVE");
        product.setRiskLevel("LOW");
        product.setReviewReason(null);
        this.updateById(product);
    }

    @Override
    @Transactional
    public void rejectProduct(Long productId, ReviewRequest request) {
        Product product = getProductOrThrow(productId);
        if (!"PENDING_REVIEW".equals(product.getStatus())) {
            throw new BusinessException("该商品不在待审核状态");
        }
        product.setStatus("REJECTED");
        product.setReviewReason(request.getReason());
        this.updateById(product);
    }

    @Override
    public Page<User> listUsers(int pageNum, int size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(User::getUsername, keyword).or()
                   .like(User::getNickname, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);

        long total = userMapper.selectCount(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<User> records = userMapper.selectList(wrapper);
        records.forEach(u -> u.setPassword(null));  // 清除密码

        Page<User> userPage = new Page<>(pageNum, size, total);
        userPage.setRecords(records);
        return userPage;
    }

    @Override
    @Transactional
    public void banUser(Long userId) {
        User user = getUserOrThrow(userId);
        if ("BANNED".equals(user.getStatus())) {
            throw new BusinessException("该用户已被封禁");
        }
        user.setStatus("BANNED");
        userMapper.updateById(user);

        // 下架该用户所有在售商品
        List<Product> activeProducts = productMapper.selectList(
                new LambdaQueryWrapper<Product>()
                        .eq(Product::getSellerId, userId)
                        .eq(Product::getStatus, "ACTIVE"));
        for (Product p : activeProducts) {
            p.setStatus("DELISTED");
            p.setReviewReason("卖家已被封禁");
            productMapper.updateById(p);
        }
    }

    @Override
    public void unbanUser(Long userId) {
        User user = getUserOrThrow(userId);
        if (!"BANNED".equals(user.getStatus())) {
            throw new BusinessException("该用户未被封禁");
        }
        user.setStatus("ACTIVE");
        userMapper.updateById(user);
    }

    @Override
    public Category createCategory(Category category) {
        categoryMapper.insert(category);
        return category;
    }

    @Override
    public Category updateCategory(Long id, Category category) {
        Category exist = categoryMapper.selectById(id);
        if (exist == null) {
            throw new BusinessException(404, "分类不存在");
        }
        exist.setName(category.getName());
        exist.setIcon(category.getIcon());
        exist.setSortOrder(category.getSortOrder());
        categoryMapper.updateById(exist);
        return exist;
    }

    @Override
    public void deleteCategory(Long id) {
        // 检查是否有商品使用此分类
        long count = productMapper.selectCount(
                new LambdaQueryWrapper<Product>().eq(Product::getCategoryId, id));
        if (count > 0) {
            throw new BusinessException("该分类下有 " + count + " 件商品，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    // ──────── FAQ 管理 ────────

    @Override
    public List<Faq> listFaqs() {
        LambdaQueryWrapper<Faq> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(Faq::getSortOrder);
        return faqMapper.selectList(wrapper);
    }

    @Override
    public Faq createFaq(Faq faq) {
        faqMapper.insert(faq);
        return faq;
    }

    @Override
    public Faq updateFaq(Long id, Faq faq) {
        Faq existing = faqMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(404, "FAQ 不存在");
        }
        existing.setQuestion(faq.getQuestion());
        existing.setAnswer(faq.getAnswer());
        existing.setSortOrder(faq.getSortOrder());
        faqMapper.updateById(existing);
        return existing;
    }

    @Override
    public void deleteFaq(Long id) {
        if (faqMapper.selectById(id) == null) {
            throw new BusinessException(404, "FAQ 不存在");
        }
        faqMapper.deleteById(id);
    }

    // ──────── 私有方法 ────────

    private Product getProductOrThrow(Long id) {
        Product product = this.getById(id);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        return product;
    }

    private User getUserOrThrow(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return user;
    }

    private List<ProductVO> toVOList(List<Product> products) {
        List<Long> sellerIds = products.stream().map(Product::getSellerId).distinct().toList();
        List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

        Map<Long, User> sellerMap = userMapper.selectBatchIds(sellerIds).stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, c -> c, (a, b) -> a));

        return products.stream().map(product -> {
            ProductVO vo = new ProductVO();
            vo.setId(product.getId());
            vo.setTitle(product.getTitle());
            vo.setDescription(product.getDescription());
            vo.setPrice(product.getPrice());
            vo.setCategoryId(product.getCategoryId());
            vo.setSellerId(product.getSellerId());
            vo.setStatus(product.getStatus());
            vo.setReviewReason(product.getReviewReason());
            vo.setRiskLevel(product.getRiskLevel());
            vo.setCoverImage(product.getCoverImage());
            vo.setContactWechat(product.getContactWechat());
            vo.setContactQq(product.getContactQq());
            vo.setCreatedAt(product.getCreatedAt());
            vo.setUpdatedAt(product.getUpdatedAt());
            vo.setSeller(sellerMap.get(product.getSellerId()));
            vo.setCategory(categoryMap.get(product.getCategoryId()));

            // 批量查图片
            List<ProductImage> images = imageMapper.selectList(
                    new LambdaQueryWrapper<ProductImage>()
                            .eq(ProductImage::getProductId, product.getId())
                            .orderByAsc(ProductImage::getSortOrder));
            vo.setImages(images);
            return vo;
        }).collect(Collectors.toList());
    }
}
