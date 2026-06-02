package com.campusmarket.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.community.dto.CreatePostRequest;
import com.campusmarket.community.dto.PostVO;
import com.campusmarket.community.entity.Post;

public interface PostService extends IService<Post> {

    /** 分页查询帖子列表（支持按主题筛选） */
    Page<PostVO> listPosts(int page, int size, Long topicId, Long currentUserId);

    /** 帖子详情 */
    PostVO getPostDetail(Long postId);

    /** 创建帖子 */
    PostVO createPost(CreatePostRequest request, Long userId);

    /** 删除帖子（仅作者可删） */
    void deletePost(Long postId, Long userId);
}
