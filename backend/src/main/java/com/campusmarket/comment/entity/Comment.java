package com.campusmarket.comment.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("product_comment")
public class Comment {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("product_id")
    private Long productId;

    @TableField("user_id")
    private Long userId;

    @TableField("content")
    private String content;

    @TableField("image_url")
    private String imageUrl;

    @TableField("video_url")
    private String videoUrl;

    @TableField("parent_id")
    private Long parentId;

    @TableField("reply_to_user_id")
    private Long replyToUserId;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
