package com.campusmarket.chat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("chat_conversation")
public class ChatConversation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String type;
    private String lastMessage;
    @TableField("last_message_at")
    private LocalDateTime lastMessageAt;
    @TableField("created_at")
    private LocalDateTime createdAt;
}
