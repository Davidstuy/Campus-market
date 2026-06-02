package com.campusmarket.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.common.response.PageResult;
import com.campusmarket.community.dto.PostCommentVO;
import com.campusmarket.community.service.PostCommentService;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/posts/{postId}/comments")
public class PostCommentController {

    private final PostCommentService postCommentService;

    /** 获取帖子评论列表（公开） */
    @GetMapping
    public ApiResult<PageResult<PostCommentVO>> list(@PathVariable Long postId,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "10") int size,
                                                       HttpServletRequest request) {
        Long currentUserId = getUserIdOrNull(request);
        Page<PostCommentVO> result = postCommentService.listByPost(postId, page, size, currentUserId);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /** 发表评论/回复（需登录） */
    @PostMapping
    public ApiResult<PostCommentVO> create(@PathVariable Long postId,
                                            @RequestBody Map<String, Object> body,
                                            HttpServletRequest request) {
        Long userId = getUserId(request);
        String content = (String) body.get("content");
        String imageUrl = (String) body.get("imageUrl");
        String videoUrl = (String) body.get("videoUrl");
        Long parentId = body.get("parentId") != null
                ? ((Number) body.get("parentId")).longValue() : null;
        Long replyToUserId = body.get("replyToUserId") != null
                ? ((Number) body.get("replyToUserId")).longValue() : null;
        PostCommentVO vo = postCommentService.create(userId, postId, content,
                imageUrl, videoUrl, parentId, replyToUserId);
        return ApiResult.success(vo);
    }

    /** 删除评论（需登录，仅作者） */
    @DeleteMapping("/{commentId}")
    public ApiResult<Void> delete(@PathVariable Long postId,
                                   @PathVariable Long commentId,
                                   HttpServletRequest request) {
        Long userId = getUserId(request);
        postCommentService.delete(commentId, userId);
        return ApiResult.success(null);
    }

    // ---------- 工具方法 ----------

    private Long getUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user.getId();
    }

    private Long getUserIdOrNull(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user != null ? user.getId() : null;
    }
}
