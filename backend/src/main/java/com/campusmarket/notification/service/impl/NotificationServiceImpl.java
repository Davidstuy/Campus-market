package com.campusmarket.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.notification.entity.Notification;
import com.campusmarket.notification.mapper.NotificationMapper;
import com.campusmarket.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification> implements NotificationService {

    @Override
    public Page<Notification> listByRecipient(Long recipientId, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientId, recipientId)
               .ne(Notification::getType, "CHAT")  // 聊天消息不显示在通知列表
               .orderByDesc(Notification::getCreatedAt);
        return this.page(new Page<>(page, size), wrapper);
    }

    @Override
    public long countUnread(Long recipientId) {
        return this.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getRecipientId, recipientId)
                .ne(Notification::getType, "CHAT")   // 排除聊天消息
                .eq(Notification::getIsRead, 0));
    }

    @Override
    public void markAsRead(Long id, Long recipientId) {
        Notification notification = this.getById(id);
        if (notification == null || !notification.getRecipientId().equals(recipientId)) {
            throw new BusinessException(404, "通知不存在");
        }
        notification.setIsRead(1);
        this.updateById(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long recipientId) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientId, recipientId)
               .eq(Notification::getIsRead, 0);
        Notification update = new Notification();
        update.setIsRead(1);
        this.update(update, wrapper);
    }

    @Override
    public void createNotification(Long recipientId, String type, String title, String content, Long orderId) {
        Notification notification = new Notification();
        notification.setRecipientId(recipientId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setOrderId(orderId);
        notification.setIsRead(0);
        this.save(notification);
    }

    @Override
    @Transactional
    public void deleteNotification(Long id, Long recipientId) {
        Notification notification = this.getById(id);
        if (notification == null || !notification.getRecipientId().equals(recipientId)) {
            throw new BusinessException(404, "通知不存在或无权操作");
        }
        // 使用 Mapper.deleteById 直接物理删除，避免全局 logic-delete-field 配置干扰
        this.getBaseMapper().deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAllNotifications(Long recipientId) {
        // 使用 Mapper.delete 直接物理删除
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notification::getRecipientId, recipientId);
        this.getBaseMapper().delete(wrapper);
    }

    @Override
    public void markChatAsRead(Long recipientId) {
        LambdaUpdateWrapper<Notification> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Notification::getRecipientId, recipientId)
               .eq(Notification::getType, "CHAT")
               .eq(Notification::getIsRead, 0);
        Notification update = new Notification();
        update.setIsRead(1);
        this.update(update, wrapper);
    }
}
