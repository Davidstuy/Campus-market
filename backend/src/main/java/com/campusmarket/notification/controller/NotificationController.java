package com.campusmarket.notification.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.notification.entity.Notification;
import com.campusmarket.notification.service.NotificationService;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long getCurrentUserId(HttpServletRequest request) {
        User user = (User) request.getAttribute("currentUser");
        return user.getId();
    }

    @GetMapping
    public ApiResult<Page<Notification>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size,
                                               HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ApiResult.success(notificationService.listByRecipient(userId, page, size));
    }

    @GetMapping("/unread-count")
    public ApiResult<Long> unreadCount(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ApiResult.success(notificationService.countUnread(userId));
    }

    @PutMapping("/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        notificationService.markAsRead(id, userId);
        return ApiResult.success();
    }

    @PutMapping("/read-all")
    public ApiResult<Void> markAllRead(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        notificationService.markAllAsRead(userId);
        return ApiResult.success();
    }

    @PutMapping("/read-chat")
    public ApiResult<Void> markChatRead(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        notificationService.markChatAsRead(userId);
        return ApiResult.success();
    }
}
