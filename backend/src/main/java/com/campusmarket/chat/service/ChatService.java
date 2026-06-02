package com.campusmarket.chat.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campusmarket.chat.dto.ChatMessageVO;
import com.campusmarket.chat.dto.ConversationVO;
import com.campusmarket.chat.dto.SendMessageRequest;
import com.campusmarket.chat.entity.ChatConversation;

import java.util.List;

public interface ChatService extends IService<ChatConversation> {

    List<ConversationVO> listConversations(Long userId);

    Page<ChatMessageVO> getMessages(Long conversationId, Long userId, int page, int size);

    ChatMessageVO sendMessage(Long senderId, SendMessageRequest request);

    void markConversationRead(Long conversationId, Long userId);

    ConversationVO getOrCreateConversation(Long buyerId, Long sellerId, Long productId);

    void deleteConversation(Long conversationId, Long userId);

    /** 获取或创建客服支持会话 */
    ConversationVO getOrCreateSupportConversation(Long userId);
}
