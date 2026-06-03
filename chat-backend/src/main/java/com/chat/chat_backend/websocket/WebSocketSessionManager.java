package com.chat.chat_backend.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket会话管理器，维护userId到WebSocketSession的映射
 * 提供线程安全的增删查操作和消息发送能力
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
public class WebSocketSessionManager {

    /** 线程安全的活跃用户会话映射表 */
    private final Map<Long, WebSocketSession> sessionMap = new ConcurrentHashMap<>();

    /**
     * 注册用户WebSocket会话
     * @param userId 认证后的用户ID
     * @param session 待注册的WebSocket会话
     */
    public void add(Long userId, WebSocketSession session) {
        if (userId != null && session != null) {
            sessionMap.put(userId, session);
        }
    }

    /**
     * 移除用户的会话映射
     * @param userId 待移除的用户ID
     */
    public void remove(Long userId) {
        if (userId != null) {
            sessionMap.remove(userId);
        }
    }

    /**
     * 获取指定用户的WebSocket会话
     * @param userId 用户ID
     * @return WebSocket会话，未连接时返回null
     */
    public WebSocketSession get(Long userId) {
        return userId == null ? null : sessionMap.get(userId);
    }

    /**
     * 检查用户是否有活跃的WebSocket会话
     * @param userId 用户ID
     * @return 用户是否在线
     */
    public boolean isOnline(Long userId) {
        return userId != null && sessionMap.containsKey(userId);
    }

    /**
     * 获取当前在线用户总数
     * @return 在线用户数
     */
    public int getOnlineCount() {
        return sessionMap.size();
    }

    /**
     * 返回所有活跃会话的只读视图
     * @return userId到WebSocketSession的映射
     */
    public Map<Long, WebSocketSession> getAllSessions() {
        return sessionMap;
    }

    /**
     * 发送消息给指定用户
     * @param userId 目标用户ID
     * @param message 消息内容
     */
    public void sendMessage(Long userId, String message) {
        WebSocketSession session = get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
                log.debug("消息已发送给用户 {}", userId);
            } catch (IOException e) {
                log.error("发送消息给用户 {} 失败: {}", userId, e.getMessage());
            }
        } else {
            log.debug("用户 {} 不在线或会话已关闭", userId);
        }
    }
}