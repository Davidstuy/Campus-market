package com.campusmarket.community.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.common.response.PageResult;
import com.campusmarket.community.dto.CreatePostRequest;
import com.campusmarket.community.dto.PostVO;
import com.campusmarket.community.service.PostService;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/posts")
public class PostController {

    private final PostService postService;

    /** 分页获取帖子列表（公开，支持 ?topicId= 筛选） */
    @GetMapping
    public ApiResult<PageResult<PostVO>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "10") int size,
                                               @RequestParam(required = false) Long topicId,
                                               HttpServletRequest request) {
        Long currentUserId = getUserIdOrNull(request);
        Page<PostVO> result = postService.listPosts(page, size, topicId, currentUserId);
        return ApiResult.success(PageResult.of(
                result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize()));
    }

    /** 帖子详情（公开） */
    @GetMapping("/{id}")
    public ApiResult<PostVO> detail(@PathVariable Long id) {
        return ApiResult.success(postService.getPostDetail(id));
    }

    /** 创建帖子（需登录） */
    @PostMapping
    public ApiResult<PostVO> create(@Valid @RequestBody CreatePostRequest request,
                                     HttpServletRequest httpRequest) {
        Long userId = getUserId(httpRequest);
        return ApiResult.success(postService.createPost(request, userId));
    }

    /** 删除帖子（需登录，仅作者） */
    @DeleteMapping("/{id}")
    public ApiResult<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getUserId(request);
        postService.deletePost(id, userId);
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
