package com.campusmarket.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    @TableField("message_type")
    private String messageType;
    @TableField("image_url")
    private String imageUrl;
    @TableField("video_url")
    private String videoUrl;
    private Integer isRead;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
