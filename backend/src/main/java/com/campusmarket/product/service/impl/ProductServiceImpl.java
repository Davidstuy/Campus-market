package com.campusmarket.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.category.entity.Category;
import com.campusmarket.category.mapper.CategoryMapper;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.product.dto.CreateProductRequest;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.product.entity.Product;
import com.campusmarket.product.entity.ProductImage;
import com.campusmarket.product.mapper.ProductImageMapper;
import com.campusmarket.product.mapper.ProductMapper;
import com.campusmarket.product.service.ProductService;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductImageMapper imageMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    /**
     * 分页查询商品列表
     *
     * LambdaQueryWrapper 的好处：
     * - Product::getTitle  而不是字符串 "title"，编译器帮你检查
     * - wrapper.eq(condition, column, value)  第一个参数为 null 时自动跳过此条件
     *
     * MyBatis-Plus 分页需要配置 PaginationInterceptor（MyBatis-Plus 3.5.x 已自动配置）
     */
    @Override
    public Page<ProductVO> listProducts(int pageNum, int size, Long categoryId, String keyword, String sort) {
        // 1. 构建查询条件
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();

        // 只查在售商品
        wrapper.eq(Product::getStatus, "ACTIVE");

        // 分类筛选：categoryId 不为空时才加此条件
        wrapper.eq(categoryId != null, Product::getCategoryId, categoryId);

        // 关键词搜索：模糊匹配标题
        wrapper.like(keyword != null && !keyword.isEmpty(), Product::getTitle, keyword);

        // 排序
        if ("price_asc".equals(sort)) {
            wrapper.orderByAsc(Product::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Product::getPrice);
        } else {
            // 默认：最新发布在前
            wrapper.orderByDesc(Product::getCreatedAt);
        }

        // 2. 先查总数，再查当前页（不依赖 MyBatis-Plus 分页拦截器）
        long total = this.count(wrapper);

        // 手动计算 offset
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<Product> records = this.list(wrapper);

        // 3. 如果没有数据，直接返回空页
        if (records.isEmpty()) {
            Page<ProductVO> emptyPage = new Page<>(pageNum, size, 0);
            emptyPage.setRecords(List.of());
            return emptyPage;
        }

        // 4. 批量查询关联数据，避免 N+1 问题

        // 收集所有 sellerId 和 categoryId
        List<Long> sellerIds = records.stream().map(Product::getSellerId).distinct().toList();
        List<Long> categoryIds = records.stream().map(Product::getCategoryId).distinct().toList();

        // 批量查询，转为 Map<id, entity> 方便后续查找
        Map<Long, User> sellerMap = userMapper.selectBatchIds(sellerIds).stream()
                .peek(u -> u.setPassword(null))  // 清除密码
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        // 4. 转换为 ProductVO
        List<ProductVO> voList = records.stream().map(product -> {
            ProductVO vo = new ProductVO();
            copyFields(product, vo);
            vo.setSeller(sellerMap.get(product.getSellerId()));
            vo.setCategory(categoryMap.get(product.getCategoryId()));
            return vo;
        }).toList();

        // 5. 构造分页结果
        Page<ProductVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 查询商品详情（含卖家、分类、图片、是否收藏）
     */
    @Override
    public ProductVO getProductDetail(Long productId) {
        Product product = this.getById(productId);
        if (product == null || !"ACTIVE".equals(product.getStatus())) {
            return null;
        }

        ProductVO vo = new ProductVO();
        copyFields(product, vo);

        // 查关联数据
        User seller = userMapper.selectById(product.getSellerId());
        if (seller != null) seller.setPassword(null);
        vo.setSeller(seller);

        vo.setCategory(categoryMapper.selectById(product.getCategoryId()));

        // 查商品图片，按 sort_order 排序
        List<ProductImage> images = imageMapper.selectList(
                new LambdaQueryWrapper<ProductImage>()
                        .eq(ProductImage::getProductId, productId)
                        .orderByAsc(ProductImage::getSortOrder)
        );
        vo.setImages(images);

        return vo;
    }

    /**
     * 发布商品
     *
     * @Transactional：为什么加事务？
     * - 一次发布操作要写 product 表 + product_image 表（多条 SQL）
     * - 如果图片写入失败，商品记录也不应该保留（要么全成功，要么全失败）
     * - 数据库事务保证 ACID 中的 A（原子性）：多条 SQL 作为一个整体执行
     */
    @Override
    @Transactional
    public ProductVO createProduct(CreateProductRequest request, Long sellerId) {
        // 1. 保存商品主记录
        Product product = new Product();
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        product.setSellerId(sellerId);
        product.setStatus("ACTIVE");
        product.setCoverImage(request.getCoverImage());
        product.setContactWechat(request.getContactWechat());
        product.setContactQq(request.getContactQq());

        this.save(product);  // MyBatis-Plus 的 save：执行 INSERT，自动回填主键 ID

        // 2. 保存图片列表（如果有）
        List<ProductImage> images = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(product.getId());
                image.setUrl(request.getImages().get(i));
                image.setSortOrder(i + 1);
                images.add(image);
            }
            imageMapper.insert(images);  // 批量插入

        }

        // 3. 拼装返回数据
        ProductVO vo = new ProductVO();
        copyFields(product, vo);
        vo.setCategory(categoryMapper.selectById(product.getCategoryId()));
        vo.setImages(images);

        User seller = userMapper.selectById(sellerId);
        if (seller != null) {
            seller.setPassword(null);
            vo.setSeller(seller);
        }

        return vo;
    }

    /**
     * 我的发布列表 — 查当前用户发布的所有商品（含已售/已下架）
     */
    @Override
    public Page<ProductVO> listMyProducts(int pageNum, int size, Long sellerId) {
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getSellerId, sellerId);
        wrapper.orderByDesc(Product::getCreatedAt);

        long total = this.count(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);

        List<Product> records = this.list(wrapper);

        if (records.isEmpty()) {
            Page<ProductVO> empty = new Page<>(pageNum, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        List<Long> categoryIds = records.stream().map(Product::getCategoryId).distinct().toList();
        Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        List<ProductVO> voList = records.stream().map(product -> {
            ProductVO vo = new ProductVO();
            copyFields(product, vo);
            vo.setCategory(categoryMap.get(product.getCategoryId()));
            return vo;
        }).toList();

        Page<ProductVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    /**
     * 编辑商品 — 校验所有权后更新字段和图片
     */
    @Override
    @Transactional
    public ProductVO updateProduct(Long productId, CreateProductRequest request, Long sellerId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "只能编辑自己的商品");
        }

        // 更新基本字段
        product.setTitle(request.getTitle());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategoryId(request.getCategoryId());
        product.setCoverImage(request.getCoverImage());
        product.setContactWechat(request.getContactWechat());
        product.setContactQq(request.getContactQq());
        this.updateById(product);

        // 更新图片：先删旧图，再插新图
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));

        List<ProductImage> images = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (int i = 0; i < request.getImages().size(); i++) {
                ProductImage image = new ProductImage();
                image.setProductId(productId);
                image.setUrl(request.getImages().get(i));
                image.setSortOrder(i + 1);
                images.add(image);
            }
            imageMapper.insert(images);
        }

        ProductVO vo = new ProductVO();
        copyFields(product, vo);
        vo.setCategory(categoryMapper.selectById(product.getCategoryId()));
        vo.setImages(images);
        return vo;
    }

    /**
     * 删除商品 — 校验所有权后删除商品和关联图片
     */
    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {
        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "只能删除自己的商品");
        }

        // 先删关联图片，再删商品
        imageMapper.delete(new LambdaQueryWrapper<ProductImage>()
                .eq(ProductImage::getProductId, productId));
        this.removeById(productId);
    }

    /**
     * 更新商品状态 — 校验所有权后修改 status 字段
     */
    @Override
    public void updateProductStatus(Long productId, String status, Long sellerId) {
        if (!"SOLD".equals(status) && !"DELISTED".equals(status)) {
            throw new BusinessException(400, "状态值无效，仅支持 SOLD 或 DELISTED");
        }

        Product product = this.getById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException(403, "只能操作自己的商品");
        }

        product.setStatus(status);
        this.updateById(product);
    }

    /**
     * 手动拷贝字段（不用 BeanUtils，因为字段名不同时要显式处理）
     * Product → ProductVO
     */
    private void copyFields(Product src, ProductVO dst) {
        dst.setId(src.getId());
        dst.setTitle(src.getTitle());
        dst.setDescription(src.getDescription());
        dst.setPrice(src.getPrice());
        dst.setCategoryId(src.getCategoryId());
        dst.setSellerId(src.getSellerId());
        dst.setStatus(src.getStatus());
        dst.setCoverImage(src.getCoverImage());
        dst.setContactWechat(src.getContactWechat());
        dst.setContactQq(src.getContactQq());
        dst.setCreatedAt(src.getCreatedAt());
        dst.setUpdatedAt(src.getUpdatedAt());
    }
}
