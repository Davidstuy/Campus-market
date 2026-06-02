package com.campusmarket.chat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campusmarket.chat.dto.ChatMessageVO;
import com.campusmarket.chat.dto.ConversationVO;
import com.campusmarket.chat.dto.SendMessageRequest;
import com.campusmarket.chat.service.ChatService;
import com.campusmarket.common.response.ApiResult;
import com.campusmarket.user.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    private Long getCurrentUserId(HttpServletRequest request) {
        return ((User) request.getAttribute("currentUser")).getId();
    }

    @GetMapping("/conversations")
    public ApiResult<List<ConversationVO>> listConversations(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ApiResult.success(chatService.listConversations(userId));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResult<Page<ChatMessageVO>> getMessages(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "1") int page,
                                                       @RequestParam(defaultValue = "30") int size,
                                                       HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ApiResult.success(chatService.getMessages(id, userId, page, size));
    }

    @PostMapping("/conversations/{id}/messages")
    public ApiResult<ChatMessageVO> sendMessage(@PathVariable Long id,
                                                  @RequestBody SendMessageRequest req,
                                                  HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        req.setConversationId(id);
        ChatMessageVO msg = chatService.sendMessage(userId, req);

        // WebSocket 实时推送
        messagingTemplate.convertAndSendToUser(
                String.valueOf(msg.getReceiverId()),
                "/queue/chat",
                msg
        );

        return ApiResult.success(msg);
    }

    @PutMapping("/conversations/{id}/read")
    public ApiResult<Void> markRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        chatService.markConversationRead(id, userId);
        return ApiResult.success();
    }

    @PostMapping("/conversations")
    public ApiResult<ConversationVO> getOrCreate(@RequestParam Long sellerId,
                                                   @RequestParam Long productId,
                                                   HttpServletRequest request) {
        Long buyerId = getCurrentUserId(request);
        return ApiResult.success(chatService.getOrCreateConversation(buyerId, sellerId, productId));
    }

    @DeleteMapping("/conversations/{id}")
    public ApiResult<Void> deleteConversation(@PathVariable Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        chatService.deleteConversation(id, userId);
        return ApiResult.success();
    }

    /** 获取或创建客服支持会话 */
    @PostMapping("/support")
    public ApiResult<ConversationVO> getOrCreateSupport(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        return ApiResult.success(chatService.getOrCreateSupportConversation(userId));
    }
}
