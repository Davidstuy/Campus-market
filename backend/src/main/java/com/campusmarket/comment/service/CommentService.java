package com.campusmarket.comment.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.comment.dto.CommentVO;
import com.campusmarket.comment.entity.Comment;

public interface CommentService extends IService<Comment> {

    /** 分页获取商品评论列表（含用户信息 + 赞踩统计 + 当前用户投票状态） */
    Page<CommentVO> listByProduct(Long productId, int page, int size, Long currentUserId);

    /** 发表评论/回复，parentId 为 null 表示顶级评论 */
    CommentVO create(Long userId, Long productId, String content, Long parentId, Long replyToUserId);

    /** 对评论投票：vote=1赞 -1踩。已投过则更新 */
    void vote(Long userId, Long commentId, int vote);

    /** 取消对评论的投票 */
    void cancelVote(Long userId, Long commentId);
}
