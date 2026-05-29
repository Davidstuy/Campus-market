package com.campusmarket.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatMessageVO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
}
