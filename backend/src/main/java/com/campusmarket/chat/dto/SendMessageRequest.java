package com.campusmarket.chat.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long conversationId;
    private Long receiverId;
    private String content;
    private String messageType;  // TEXT / IMAGE / VIDEO，默认 TEXT
    private String imageUrl;     // IMAGE 类型时使用
    private String videoUrl;     // VIDEO 类型时使用
}
