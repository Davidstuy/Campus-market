package com.campusmarket.chat.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ConversationVO {
    private Long id;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String type;
    private String productTitle;
    private String productCover;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
    private long unreadCount;
    private String otherPartyName;
    private String otherPartyAvatar;
    private LocalDateTime createdAt;
}
