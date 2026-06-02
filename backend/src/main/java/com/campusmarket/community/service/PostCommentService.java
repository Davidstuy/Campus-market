package com.campusmarket.community.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.community.dto.PostCommentVO;
import com.campusmarket.community.entity.PostComment;

public interface PostCommentService extends IService<PostComment> {

    /** 分页查询帖子的评论（含嵌套回复） */
    Page<PostCommentVO> listByPost(Long postId, int page, int size, Long currentUserId);

    /** 创建评论或回复 */
    PostCommentVO create(Long userId, Long postId, String content,
                         String imageUrl, String videoUrl,
                         Long parentId, Long replyToUserId);

    /** 删除评论（仅作者可删） */
    void delete(Long commentId, Long userId);
}
