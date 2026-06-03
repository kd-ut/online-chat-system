package com.chat.chat_backend.websocket.handler;

import com.chat.chat_backend.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.net.URI;

/**
 * WebSocket连接认证拦截器，从查询字符串中提取并验证JWT令牌
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor {

    /** JWT工具类，用于令牌验证和声明提取 */
    private final JwtUtil jwtUtil;

    /**
     * 从WebSocket URI的查询参数"token"中提取令牌并验证，返回认证后的用户ID
     * @param session 待认证的WebSocket会话
     * @return 认证成功返回用户ID，失败返回null
     */
    public Long authenticate(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            log.warn("URI为空");
            return null;
        }

        // 提取查询参数中的token
        String query = uri.getQuery();
        if (query == null || !query.contains("token=")) {
            log.warn("Query中无token");
            return null;
        }

        // 解析token值（可能包含其他参数）
        String token = query.split("token=")[1];
        if (token.contains("&")) {
            token = token.split("&")[0];
        }

        // 验证令牌并返回用户ID
        if (jwtUtil.validateToken(token)) {
            return jwtUtil.getUserIdFromToken(token);
        }

        log.warn("Token验证失败");
        return null;
    }
}