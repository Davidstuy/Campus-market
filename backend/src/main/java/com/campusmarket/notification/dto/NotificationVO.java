package com.campusmarket.notification.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private String type;
    private String title;
    private String content;
    private Long orderId;
    private Integer isRead;
    private LocalDateTime createdAt;
}
