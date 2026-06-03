package com.chat.chat_backend.config;

import com.chat.chat_backend.websocket.ChatWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类，将聊天WebSocket处理器注册到/ws端点
 * @author chat-backend
 * @since 2026-05-12
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    /** 聊天WebSocket处理器，处理消息和信令 */
    private final ChatWebSocketHandler chatWebSocketHandler;

    /**
     * 注册聊天WebSocket处理器到/ws端点，允许所有来源访问
     * @param registry WebSocket处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws")
                .setAllowedOrigins("*");
    }
}