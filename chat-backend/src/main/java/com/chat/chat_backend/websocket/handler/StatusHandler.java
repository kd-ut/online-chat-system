package com.chat.chat_backend.websocket.handler;

import cn.hutool.json.JSONObject;
import com.chat.chat_backend.common.constant.RedisConstants;
import com.chat.chat_backend.common.utils.RedisUtil;
import com.chat.chat_backend.websocket.WebSocketSessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 用户在线/离线状态处理器，在Redis中维护状态并向所有已连接用户广播
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatusHandler {

    /** Redis工具类，用于持久化在线状态 */
    private final RedisUtil redisUtil;

    /** 会话管理器，用于向所有客户端广播状态 */
    private final WebSocketSessionManager sessionManager;

    /**
     * 将用户标记为在线（加入Redis在线集合并设置过期时间）
     * @param userId 上线的用户ID
     */
    public void userOnline(Long userId) {
        redisUtil.addToSet(RedisConstants.ONLINE_USERS, String.valueOf(userId));
        redisUtil.expire(RedisConstants.ONLINE_USERS, RedisConstants.ONLINE_EXPIRE_SECONDS, TimeUnit.SECONDS);
    }

    /**
     * 将用户标记为离线（从Redis在线集合中移除）
     * @param userId 离线的用户ID
     */
    public void userOffline(Long userId) {
        redisUtil.removeFromSet(RedisConstants.ONLINE_USERS, String.valueOf(userId));
    }

    /**
     * 向所有已连接的WebSocket会话广播用户的在线/离线状态
     * @param userId 状态变更的用户ID
     * @param online true表示上线，false表示离线
     */
    public void broadcastStatus(Long userId, boolean online) {
        JSONObject statusMsg = new JSONObject();
        statusMsg.set("type", "status");
        statusMsg.set("userId", userId);
        statusMsg.set("online", online);

        String jsonMsg = statusMsg.toString();

        for (WebSocketSession session : sessionManager.getAllSessions().values()) {
            if (session != null && session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(jsonMsg));
                } catch (IOException e) {
                    log.warn("广播状态失败: {}", e.getMessage());
                }
            }
        }
    }
}