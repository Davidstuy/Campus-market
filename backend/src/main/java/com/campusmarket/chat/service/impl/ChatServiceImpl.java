
package com.campusmarket.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campusmarket.chat.dto.ChatMessageVO;
import com.campusmarket.chat.dto.ConversationVO;
import com.campusmarket.chat.dto.SendMessageRequest;
import com.campusmarket.chat.entity.ChatConversation;
import com.campusmarket.chat.entity.ChatMessage;
import com.campusmarket.chat.mapper.ChatConversationMapper;
import com.campusmarket.chat.mapper.ChatMessageMapper;
import com.campusmarket.chat.service.ChatService;
import com.campusmarket.common.exception.BusinessException;
import com.campusmarket.notification.dto.NotificationVO;
import com.campusmarket.notification.service.NotificationPushService;
import com.campusmarket.notification.service.NotificationService;
import com.campusmarket.product.entity.Product;
import com.campusmarket.product.mapper.ProductMapper;
import com.campusmarket.user.entity.User;
import com.campusmarket.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatConversationMapper, ChatConversation> implements ChatService {

    private final ChatMessageMapper chatMessageMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;
    private final NotificationPushService notificationPushService;

    @Override
    public List<ConversationVO> listConversations(Long userId) {
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getBuyerId, userId)
               .or()
               .eq(ChatConversation::getSellerId, userId)
               .orderByDesc(ChatConversation::getLastMessageAt);
        List<ChatConversation> conversations = this.list(wrapper);

        if (conversations.isEmpty()) return List.of();

        // 批量查用户
        List<Long> userIds = new ArrayList<>();
        for (ChatConversation c : conversations) {
            Long otherId = c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId();
            userIds.add(otherId);
        }
        Map<Long, User> userMap = userMapper.selectBatchIds(userIds).stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // 批量查商品
        List<Long> productIds = conversations.stream()
                .map(ChatConversation::getProductId).distinct().toList();
        Map<Long, Product> productMap = productMapper.selectBatchIds(productIds).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // 批量查未读数
        List<Long> convIds = conversations.stream().map(ChatConversation::getId).toList();
        Map<Long, Long> unreadMap = countUnreadByConversations(convIds, userId);

        return conversations.stream().map(c -> {
            ConversationVO vo = new ConversationVO();
            vo.setId(c.getId());
            vo.setBuyerId(c.getBuyerId());
            vo.setSellerId(c.getSellerId());
            vo.setProductId(c.getProductId());
            vo.setLastMessage(c.getLastMessage());
            vo.setLastMessageAt(c.getLastMessageAt());
            vo.setCreatedAt(c.getCreatedAt());

            Long otherId = c.getBuyerId().equals(userId) ? c.getSellerId() : c.getBuyerId();
            User other = userMap.get(otherId);
            if (other != null) {
                String nick = other.getNickname();
                vo.setOtherPartyName(nick != null && !nick.isEmpty() ? nick : other.getUsername());
                vo.setOtherPartyAvatar(other.getAvatarUrl());
            }

            Product p = productMap.get(c.getProductId());
            if (p != null) {
                vo.setProductTitle(p.getTitle());
                vo.setProductCover(p.getCoverImage());
            }

            vo.setUnreadCount(unreadMap.getOrDefault(c.getId(), 0L));
            return vo;
        }).toList();
    }

    @Override
    public Page<ChatMessageVO> getMessages(Long conversationId, Long userId, int page, int size) {
        ChatConversation conversation = this.getById(conversationId);
        if (conversation == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!conversation.getBuyerId().equals(userId) && !conversation.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权查看此会话");
        }

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .orderByDesc(ChatMessage::getCreatedAt);

        Page<ChatMessage> msgPage = chatMessageMapper.selectPage(new Page<>(page, size), wrapper);

        List<ChatMessageVO> vos = msgPage.getRecords().stream().map(m -> {
            ChatMessageVO vo = new ChatMessageVO();
            vo.setId(m.getId());
            vo.setConversationId(m.getConversationId());
            vo.setSenderId(m.getSenderId());
            vo.setReceiverId(m.getReceiverId());
            vo.setContent(m.getContent());
            vo.setIsRead(m.getIsRead());
            vo.setCreatedAt(m.getCreatedAt());
            return vo;
        }).toList();

        Page<ChatMessageVO> voPage = new Page<>(page, size, msgPage.getTotal());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    @Transactional
    public ChatMessageVO sendMessage(Long senderId, SendMessageRequest request) {
        ChatConversation conversation = this.getById(request.getConversationId());
        if (conversation == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!conversation.getBuyerId().equals(senderId) && !conversation.getSellerId().equals(senderId)) {
            throw new BusinessException(403, "无权在此会话发消息");
        }

        Long receiverId = request.getReceiverId();

        ChatMessage msg = new ChatMessage();
        msg.setConversationId(request.getConversationId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(request.getContent());
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        // 更新会话最后一条消息
        conversation.setLastMessage(request.getContent());
        conversation.setLastMessageAt(LocalDateTime.now());
        this.updateById(conversation);

        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setReceiverId(msg.getReceiverId());
        vo.setContent(msg.getContent());
        vo.setIsRead(msg.getIsRead());
        vo.setCreatedAt(msg.getCreatedAt());

        // 为接收者创建通知
        notifyNewMessage(senderId, receiverId, request.getContent());

        return vo;
    }

    @Override
    public void markConversationRead(Long conversationId, Long userId) {
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
               .eq(ChatMessage::getReceiverId, userId)
               .eq(ChatMessage::getIsRead, 0);
        ChatMessage update = new ChatMessage();
        update.setIsRead(1);
        chatMessageMapper.update(update, wrapper);
    }

    @Override
    @Transactional
    public ConversationVO getOrCreateConversation(Long buyerId, Long sellerId, Long productId) {
        if (buyerId.equals(sellerId)) {
            throw new BusinessException(400, "不能与自己聊天");
        }

        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getBuyerId, buyerId)
               .eq(ChatConversation::getSellerId, sellerId)
               .eq(ChatConversation::getProductId, productId);
        ChatConversation existing = this.getOne(wrapper);

        if (existing != null) {
            ConversationVO vo = new ConversationVO();
            vo.setId(existing.getId());
            vo.setBuyerId(existing.getBuyerId());
            vo.setSellerId(existing.getSellerId());
            vo.setProductId(existing.getProductId());
            vo.setLastMessage(existing.getLastMessage());
            vo.setLastMessageAt(existing.getLastMessageAt());
            vo.setCreatedAt(existing.getCreatedAt());
            return vo;
        }

        ChatConversation conv = new ChatConversation();
        conv.setBuyerId(buyerId);
        conv.setSellerId(sellerId);
        conv.setProductId(productId);
        conv.setLastMessage("");
        this.save(conv);

        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setBuyerId(conv.getBuyerId());
        vo.setSellerId(conv.getSellerId());
        vo.setProductId(conv.getProductId());
        vo.setCreatedAt(conv.getCreatedAt());
        return vo;
    }

    private Map<Long, Long> countUnreadByConversations(List<Long> convIds, Long userId) {
        if (convIds.isEmpty()) return Map.of();
        List<ChatMessage> msgs = chatMessageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .in(ChatMessage::getConversationId, convIds)
                        .eq(ChatMessage::getReceiverId, userId)
                        .eq(ChatMessage::getIsRead, 0));
        return msgs.stream()
                .collect(Collectors.groupingBy(ChatMessage::getConversationId, Collectors.counting()));
    }

    private void notifyNewMessage(Long senderId, Long receiverId, String content) {
        User sender = userMapper.selectById(senderId);
        String senderName = sender != null
                ? (sender.getNickname() != null && !sender.getNickname().isEmpty()
                    ? sender.getNickname()
                    : sender.getUsername())
                : "用户";

        String preview = content.length() > 50 ? content.substring(0, 50) + "..." : content;

        String title = senderName + " 发来新消息";

        notificationService.createNotification(receiverId, "CHAT", title, preview, null);

        NotificationVO vo = new NotificationVO();
        vo.setType("CHAT");
        vo.setTitle(title);
        vo.setContent(preview);
        vo.setIsRead(0);
        notificationPushService.pushNotification(String.valueOf(receiverId), vo);
    }
}
