package com.campusmarket.chat.dto;

import lombok.Data;

@Data
public class SendMessageRequest {
    private Long conversationId;
    private Long receiverId;
    private String content;
}
