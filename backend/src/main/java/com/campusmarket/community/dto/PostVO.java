package com.campusmarket.community.dto;

import com.campusmarket.community.entity.PostMedia;
import com.campusmarket.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private Long userId;
    private Long topicId;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 作者信息 */
    private User user;

    /** 主题信息 */
    private TopicVO topic;

    /** 媒体列表（图片+视频混合） */
    private List<PostMedia> media;

    /** 评论数 */
    private Long commentCount;
}
