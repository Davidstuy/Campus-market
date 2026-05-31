package com.campusmarket.comment.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.comment.dto.CommentVO;
import com.campusmarket.comment.service.CommentService;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.common.response.PageResult;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/products/{productId}/comments")
public class CommentController {

    private final CommentService commentService;

    /** 获取商品评论列表（公开，已登录用户会带 myVote） */
    @GetMapping
    public ApiResult<PageResult<CommentVO>> list(@PathVariable Long productId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size,
                                                  HttpServletRequest request) {
        Long currentUserId = getUserIdOrNull(request);
        Page<CommentVO> result = commentService.listByProduct(productId, page, size, currentUserId);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /** 发表评论/回复（需登录） */
    @PostMapping
    public ApiResult<CommentVO> create(@PathVariable Long productId,
                                        @RequestBody Map<String, Object> body,
                                        HttpServletRequest request) {
        Long userId = getUserId(request);
        String content = (String) body.get("content");
        Long parentId = body.get("parentId") != null
                ? ((Number) body.get("parentId")).longValue() : null;
        Long replyToUserId = body.get("replyToUserId") != null
                ? ((Number) body.get("replyToUserId")).longValue() : null;
        CommentVO vo = commentService.create(userId, productId, content, parentId, replyToUserId);
        return ApiResult.success(vo);
    }

    /** 对评论投票（需登录）：body { "vote": 1 } 赞，{ "vote": -1 } 踩 */
    @PutMapping("/{commentId}/vote")
    public ApiResult<Void> vote(@PathVariable Long productId,
                                 @PathVariable Long commentId,
                                 @RequestBody Map<String, Integer> body,
                                 HttpServletRequest request) {
        Long userId = getUserId(request);
        Integer vote = body.get("vote");
        if (vote == null) {
            vote = 1; // 默认赞
        }
        commentService.vote(userId, commentId, vote);
        return ApiResult.success(null);
    }

    /** 取消投票（需登录） */
    @DeleteMapping("/{commentId}/vote")
    public ApiResult<Void> cancelVote(@PathVariable Long productId,
                                       @PathVariable Long commentId,
                                       HttpServletRequest request) {
        Long userId = getUserId(request);
        commentService.cancelVote(userId, commentId);
        return ApiResult.success(null);
    }

    /** 获取已登录用户 ID（必须登录） */
    private Long getUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user.getId();
    }

    /** 获取当前用户 ID（未登录返回 null） */
    private Long getUserIdOrNull(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getId() : null;
    }
}
