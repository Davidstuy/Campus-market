package com.campusmarket.favorite.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.common.response.PageResult;
import com.campusmarket.favorite.service.FavoriteService;
import com.campusmarket.product.dto.ProductVO;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    /** 收藏商品 */
    @PostMapping
    public ApiResult<Map<String, Boolean>> add(@RequestBody Map<String, Long> body,
                                                HttpServletRequest request) {
        Long userId = getUserId(request);
        Long productId = body.get("productId");
        boolean added = favoriteService.add(userId, productId);
        return ApiResult.success(Map.of("favorited", added));
    }

    /** 取消收藏（URL 传 productId） */
    @DeleteMapping("/{productId}")
    public ApiResult<Void> remove(@PathVariable Long productId,
                                  HttpServletRequest request) {
        Long userId = getUserId(request);
        favoriteService.remove(userId, productId);
        return ApiResult.success(null);
    }

    /** 收藏列表（分页） */
    @GetMapping
    public ApiResult<PageResult<ProductVO>> list(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "12") int size,
                                                  HttpServletRequest request) {
        Long userId = getUserId(request);
        Page<ProductVO> result = favoriteService.listFavorites(userId, page, size);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /** 批量检查收藏状态（POST body: { "productIds": [1, 2, 3] }） */
    @PostMapping("/check")
    public ApiResult<Map<Long, Boolean>> check(@RequestBody Map<String, List<Long>> body,
                                                HttpServletRequest request) {
        Long userId = getUserId(request);
        List<Long> ids = body.getOrDefault("productIds", List.of());
        Map<Long, Boolean> result = favoriteService.checkFavorites(userId, new HashSet<>(ids));
        return ApiResult.success(result);
    }

    private Long getUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user.getId();
    }
}
