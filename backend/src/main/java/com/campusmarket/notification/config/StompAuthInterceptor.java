package com.campusmarket.notification.config;

import com.campusmarket.auth.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * STOMP 认证拦截器 — 在 WebSocket CONNECT 时从 header 中提取 JWT 并设置 Principal
 *
 * 只有设置了 Principal，convertAndSendToUser() 才能找到对应用户的 WebSocket 会话，
 * 否则消息会被静默丢弃。
 */
@Component
@RequiredArgsConstructor
public class StompAuthInterceptor implements ChannelInterceptor {

    private final JwtProvider jwtProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (jwtProvider.validateToken(token)) {
                    Long userId = jwtProvider.getUserIdFromToken(token);
                    accessor.setUser(new StompPrincipal(String.valueOf(userId)));
                }
            }
        }

        return message;
    }

    private record StompPrincipal(String name) implements Principal {
        @Override
        public String getName() {
            return name;
        }
    }
}
