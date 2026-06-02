package com.campusmarket.community.dto;

import com.campusmarket.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostCommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String content;
    private String imageUrl;
    private String videoUrl;
    private Long parentId;
    private Long replyToUserId;
    private LocalDateTime createdAt;

    /** 评论者信息 */
    private User user;

    /** 被回复者信息（仅回复时有值） */
    private User replyToUser;

    /** 子回复列表 */
    private List<PostCommentVO> replies;
}
