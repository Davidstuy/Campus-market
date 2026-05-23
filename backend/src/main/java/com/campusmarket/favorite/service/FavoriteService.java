package com.campusmarket.favorite.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.favorite.entity.Favorite;
import com.campusmarket.product.dto.ProductVO;

import java.util.Map;
import java.util.Set;

public interface FavoriteService extends IService<Favorite> {

    /** 收藏商品（已收藏则忽略，返回 true=收藏成功 false=已收藏） */
    boolean add(Long userId, Long productId);

    /** 取消收藏 */
    void remove(Long userId, Long productId);

    /** 收藏列表（分页，含商品信息） */
    Page<ProductVO> listFavorites(Long userId, int page, int size);

    /** 批量检查哪些商品被当前用户收藏，返回 {productId: true} */
    Map<Long, Boolean> checkFavorites(Long userId, Set<Long> productIds);
}
