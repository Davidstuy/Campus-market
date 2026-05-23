package com.campusmarket.product.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.product.dto.CreateProductRequest;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.product.entity.Product;

/**
 * 商品业务接口
 */
public interface ProductService extends IService<Product> {

    /**
     * 分页查询商品列表（支持分类筛选、关键词搜索、排序）
     */
    Page<ProductVO> listProducts(int page, int size, Long categoryId, String keyword, String sort);

    /**
     * 查询商品详情（含卖家信息、分类信息、图片列表）
     */
    ProductVO getProductDetail(Long productId);

    /**
     * 发布商品（含图片）
     */
    ProductVO createProduct(CreateProductRequest request, Long sellerId);

    /**
     * 我的发布列表（含所有状态：在售、已售、已下架）
     */
    Page<ProductVO> listMyProducts(int page, int size, Long sellerId);

    /**
     * 编辑商品（只能编辑自己的商品）
     */
    ProductVO updateProduct(Long productId, CreateProductRequest request, Long sellerId);

    /**
     * 删除商品（只能删除自己的商品）
     */
    void deleteProduct(Long productId, Long sellerId);

    /**
     * 更新商品状态（ACTIVE → SOLD / DELISTED，只能操作自己的商品）
     */
    void updateProductStatus(Long productId, String status, Long sellerId);
}
