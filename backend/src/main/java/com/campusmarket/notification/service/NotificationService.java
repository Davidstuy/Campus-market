package com.campusmarket.notification.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.notification.entity.Notification;

public interface NotificationService extends IService<Notification> {

    Page<Notification> listByRecipient(Long recipientId, int page, int size);

    long countUnread(Long recipientId);

    void markAsRead(Long id, Long recipientId);

    void markAllAsRead(Long recipientId);

    void createNotification(Long recipientId, String type, String title, String content, Long orderId);

    void markChatAsRead(Long recipientId);
}
