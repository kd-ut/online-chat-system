package com.chat.chat_backend.websocket;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.chat.chat_backend.websocket.handler.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * 主要WebSocket处理器，管理连接生命周期、消息分发和状态广播
 * 根据消息类型将消息路由到对应的子处理器
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    /** 用户会话管理器 */
    private final WebSocketSessionManager sessionManager;

    /** WebSocket JWT认证拦截器 */
    private final WebSocketAuthInterceptor authInterceptor;

    /** 私聊消息处理器 */
    private final MessageHandler messageHandler;

    /** 群聊消息处理器 */
    private final GroupMessageHandler groupMessageHandler;

    /** WebRTC通话信令处理器 */
    private final CallSignalHandler callSignalHandler;

    /** 在线/离线状态广播处理器 */
    private final StatusHandler statusHandler;

    /**
     * WebSocket连接建立后的回调
     * 认证会话、注册到管理器、广播在线状态
     * @param session WebSocket会话
     * @throws Exception 认证或会话处理失败时抛出
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // 认证JWT令牌
        Long userId = authInterceptor.authenticate(session);
        if (userId == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        // 注册会话并广播上线状态
        sessionManager.add(userId, session);
        session.getAttributes().put("userId", userId);
        statusHandler.userOnline(userId);
        statusHandler.broadcastStatus(userId, true);

        log.info("用户 {} 已连接WebSocket，当前在线人数: {}", userId, sessionManager.getOnlineCount());
    }

    /**
     * 根据消息type字段将消息分发到对应的子处理器
     * 支持类型：message（私聊）、group_message（群聊）、call（通话信令）、ping（心跳）
     * @param session WebSocket会话
     * @param message 收到的文本消息
     * @throws Exception 消息处理失败时抛出
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId == null) return;

        String payload = message.getPayload();
        JSONObject json = JSONUtil.parseObj(payload);
        String type = json.getStr("type");

        switch (type) {
            case "message":
                messageHandler.handle(userId, json);
                break;
            case "group_message":
                groupMessageHandler.handle(userId, json);
                break;
            case "call":
                callSignalHandler.handle(userId, json);
                break;
            case "ping":
                session.sendMessage(new TextMessage("{\"type\":\"pong\"}"));
                break;
            default:
                log.warn("未知消息类型: {}", type);
        }
    }

    /**
     * WebSocket连接关闭后的回调
     * 从管理器中移除会话并广播离线状态
     * @param session 已关闭的WebSocket会话
     * @param status 关闭状态
     * @throws Exception 清理操作失败时抛出
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        if (userId != null) {
            sessionManager.remove(userId);
            statusHandler.userOffline(userId);
            statusHandler.broadcastStatus(userId, false);
            log.info("用户 {} 已断开WebSocket", userId);
        }
    }
}