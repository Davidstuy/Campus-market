
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

        // 过滤掉自聊会话（buyerId == sellerId，如管理员跟自己聊天）
        conversations = conversations.stream()
                .filter(c -> !c.getBuyerId().equals(c.getSellerId()))
                .toList();
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
            vo.setType(c.getType());
            vo.setLastMessage(c.getLastMessage());
            vo.setLastMessageAt(c.getLastMessageAt());
            vo.setCreatedAt(c.getCreatedAt());

            if ("SUPPORT".equals(c.getType())) {
                // 客服会话：对方显示为"平台客服"
                vo.setOtherPartyName("平台客服");
                User adminUser = userMap.get(c.getSellerId());
                vo.setOtherPartyAvatar(adminUser != null ? adminUser.getAvatarUrl() : "");
            } else {
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
            vo.setMessageType(m.getMessageType());
            vo.setImageUrl(m.getImageUrl());
            vo.setVideoUrl(m.getVideoUrl());
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

        // 禁止自聊（SUPPORT 会话的防御性检查）
        if (conversation.getBuyerId().equals(conversation.getSellerId())) {
            throw new BusinessException(400, "会话异常，请重新发起客服咨询");
        }

        Long receiverId = request.getReceiverId();
        String messageType = request.getMessageType() != null ? request.getMessageType() : "TEXT";

        // 验证：IMAGE / VIDEO 类型必须有对应的 URL
        if ("IMAGE".equals(messageType) && (request.getImageUrl() == null || request.getImageUrl().isBlank())) {
            throw new BusinessException(400, "图片消息必须提供 imageUrl");
        }
        if ("VIDEO".equals(messageType) && (request.getVideoUrl() == null || request.getVideoUrl().isBlank())) {
            throw new BusinessException(400, "视频消息必须提供 videoUrl");
        }

        ChatMessage msg = new ChatMessage();
        msg.setConversationId(request.getConversationId());
        msg.setSenderId(senderId);
        msg.setReceiverId(receiverId);
        msg.setContent(request.getContent() != null ? request.getContent() : "");
        msg.setMessageType(messageType);
        msg.setImageUrl(request.getImageUrl());
        msg.setVideoUrl(request.getVideoUrl());
        msg.setIsRead(0);
        chatMessageMapper.insert(msg);

        // 更新会话最后一条消息
        String lastMsgPreview = buildLastMessagePreview(messageType, request.getContent(), request.getImageUrl(), request.getVideoUrl());
        conversation.setLastMessage(lastMsgPreview);
        conversation.setLastMessageAt(LocalDateTime.now());
        this.updateById(conversation);

        ChatMessageVO vo = new ChatMessageVO();
        vo.setId(msg.getId());
        vo.setConversationId(msg.getConversationId());
        vo.setSenderId(msg.getSenderId());
        vo.setReceiverId(msg.getReceiverId());
        vo.setContent(msg.getContent());
        vo.setMessageType(msg.getMessageType());
        vo.setImageUrl(msg.getImageUrl());
        vo.setVideoUrl(msg.getVideoUrl());
        vo.setIsRead(msg.getIsRead());
        vo.setCreatedAt(msg.getCreatedAt());

        // 为接收者创建通知
        notifyNewMessage(senderId, receiverId, lastMsgPreview);

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
    public void deleteConversation(Long conversationId, Long userId) {
        ChatConversation conv = this.getById(conversationId);
        if (conv == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!conv.getBuyerId().equals(userId) && !conv.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权删除此会话");
        }
        // 级联删除消息
        chatMessageMapper.delete(new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getConversationId, conversationId));
        this.removeById(conversationId);
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
               .eq(ChatConversation::getType, "PRODUCT");
        ChatConversation existing = this.getOne(wrapper);

        if (existing != null) {
            ConversationVO vo = new ConversationVO();
            vo.setId(existing.getId());
            vo.setBuyerId(existing.getBuyerId());
            vo.setSellerId(existing.getSellerId());
            vo.setProductId(existing.getProductId());
            vo.setType(existing.getType());
            vo.setLastMessage(existing.getLastMessage());
            vo.setLastMessageAt(existing.getLastMessageAt());
            vo.setCreatedAt(existing.getCreatedAt());
            return vo;
        }

        ChatConversation conv = new ChatConversation();
        conv.setBuyerId(buyerId);
        conv.setSellerId(sellerId);
        conv.setProductId(productId);
        conv.setType("PRODUCT");
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

    @Override
    @Transactional
    public ConversationVO getOrCreateSupportConversation(Long userId) {
        // 查找管理员（排除当前用户自己）
        LambdaQueryWrapper<User> adminQuery = new LambdaQueryWrapper<>();
        adminQuery.eq(User::getRole, "ADMIN")
                  .eq(User::getStatus, "ACTIVE")
                  .ne(User::getId, userId)       // 排除当前用户（管理员不能跟自己聊天）
                  .orderByAsc(User::getId)
                  .last("LIMIT 1");
        User admin = userMapper.selectOne(adminQuery);
        if (admin == null) {
            throw new BusinessException(500, "客服系统暂不可用，请稍后再试");
        }

        // 查找已有的 SUPPORT 会话
        LambdaQueryWrapper<ChatConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatConversation::getBuyerId, userId)
               .eq(ChatConversation::getSellerId, admin.getId())
               .eq(ChatConversation::getProductId, 0L)
               .eq(ChatConversation::getType, "SUPPORT");
        ChatConversation existing = this.getOne(wrapper);

        if (existing != null) {
            return buildSupportConversationVO(existing, admin);
        }

        // 创建新会话
        ChatConversation conv = new ChatConversation();
        conv.setBuyerId(userId);
        conv.setSellerId(admin.getId());
        conv.setProductId(0L);
        conv.setType("SUPPORT");
        conv.setLastMessage("");
        this.save(conv);

        return buildSupportConversationVO(conv, admin);
    }

    private ConversationVO buildSupportConversationVO(ChatConversation conv, User admin) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setBuyerId(conv.getBuyerId());
        vo.setSellerId(conv.getSellerId());
        vo.setProductId(conv.getProductId());
        vo.setType("SUPPORT");
        vo.setOtherPartyName("平台客服");
        vo.setOtherPartyAvatar(admin.getAvatarUrl());
        vo.setLastMessage(conv.getLastMessage());
        vo.setLastMessageAt(conv.getLastMessageAt());
        vo.setCreatedAt(conv.getCreatedAt());
        vo.setUnreadCount(0);
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

    /** 根据消息类型构建会话列表预览文字 */
    private String buildLastMessagePreview(String messageType, String content, String imageUrl, String videoUrl) {
        return switch (messageType) {
            case "IMAGE" -> "[图片]";
            case "VIDEO" -> "[视频]";
            default -> {
                if (content == null || content.isEmpty()) yield "";
                yield content.length() > 50 ? content.substring(0, 50) + "..." : content;
            }
        };
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
