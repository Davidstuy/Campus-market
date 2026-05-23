package com.campusmarket.favorite.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.category.entity.Category;
import com.campusmarket.category.mapper.CategoryMapper;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.favorite.entity.Favorite;
import com.campusmarket.favorite.mapper.FavoriteMapper;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.product.entity.Product;
import com.campusmarket.product.entity.ProductImage;
import com.campusmarket.product.mapper.ProductImageMapper;
import com.campusmarket.product.mapper.ProductMapper;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    private final ProductMapper productMapper;
    private final ProductImageMapper imageMapper;
    private final UserMapper userMapper;
    private final CategoryMapper categoryMapper;

    @Override
    public boolean add(Long userId, Long productId) {
        // 检查商品是否存在且为在售状态
        Product product = productMapper.selectById(productId);
        if (product == null || !"ACTIVE".equals(product.getStatus())) {
            throw new BusinessException(404, "商品不存在或已下架");
        }

        // 检查是否已收藏（UNIQUE KEY 会阻止重复，但这里先查一次给友好提示）
        Long count = this.count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
        if (count > 0) {
            return false; // 已收藏
        }

        Favorite fav = new Favorite();
        fav.setUserId(userId);
        fav.setProductId(productId);
        this.save(fav);
        return true;
    }

    @Override
    public void remove(Long userId, Long productId) {
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId);
        this.remove(wrapper);
    }

    @Override
    public Page<ProductVO> listFavorites(Long userId, int pageNum, int size) {
        // 1. 查收藏记录（分页）
        LambdaQueryWrapper<Favorite> wrapper = new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreatedAt);

        long total = this.count(wrapper);
        int offset = (pageNum - 1) * size;
        wrapper.last("LIMIT " + offset + ", " + size);
        List<Favorite> favList = this.list(wrapper);

        if (favList.isEmpty()) {
            Page<ProductVO> empty = new Page<>(pageNum, size, 0);
            empty.setRecords(List.of());
            return empty;
        }

        // 2. 批量查出所有关联商品
        List<Long> productIds = favList.stream().map(Favorite::getProductId).toList();
        List<Product> products = productMapper.selectBatchIds(productIds);

        // 建 productId → Product 映射
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 3. 批量查询关联数据
        List<Long> sellerIds = products.stream().map(Product::getSellerId).distinct().toList();
        List<Long> categoryIds = products.stream().map(Product::getCategoryId).distinct().toList();

        Map<Long, User> sellerMap = userMapper.selectBatchIds(sellerIds).stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Category> categoryMap = categoryMapper.selectBatchIds(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Function.identity()));

        // 4. 按收藏顺序组装 ProductVO
        List<ProductVO> voList = favList.stream().map(fav -> {
            Product product = productMap.get(fav.getProductId());
            if (product == null) return null;

            ProductVO vo = new ProductVO();
            copyProductFields(product, vo);
            vo.setSeller(sellerMap.get(product.getSellerId()));
            vo.setCategory(categoryMap.get(product.getCategoryId()));
            vo.setIsFavorited(true);
            return vo;
        }).filter(Objects::nonNull).toList();

        Page<ProductVO> voPage = new Page<>(pageNum, size, total);
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Map<Long, Boolean> checkFavorites(Long userId, Set<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        // 查当前用户在 productIds 中有哪些收藏
        List<Favorite> favs = this.list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .in(Favorite::getProductId, productIds));

        // 转为 Map<productId, true>
        Map<Long, Boolean> result = new HashMap<>();
        for (Favorite fav : favs) {
            result.put(fav.getProductId(), true);
        }

        // 未收藏的返回 false（前端 Map 查找时需要）
        for (Long pid : productIds) {
            result.putIfAbsent(pid, false);
        }

        return result;
    }

    private void copyProductFields(Product src, ProductVO dst) {
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
