package com.campusmarket.comment.dto;

import com.campusmarket.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long productId;
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
    private List<CommentVO> replies;

    /** 赞数 */
    private long upCount;

    /** 踩数 */
    private long downCount;

    /** 当前用户的投票：1=赞 -1=踩 null=未投票 */
    private Integer myVote;
}
