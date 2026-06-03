package com.chat.chat_backend.interceptor;

import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.JwtUtil;
import com.chat.chat_backend.common.constant.RedisConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP请求拦截器，从Authorization头中验证JWT令牌
 * 检查令牌格式、黑名单状态和有效性，通过后放行请求
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    /** JWT工具类，用于令牌验证和解析 */
    private final JwtUtil jwtUtil;

    /** Redis模板，用于检查令牌黑名单 */
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 前置拦截方法，验证请求中的JWT Bearer令牌
     * 拒绝缺失、格式错误、已被黑名单或过期的令牌
     * 验证通过后将userId和role设置为请求属性
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器对象
     * @return 请求是否继续（true放行，false拦截）
     * @throws Exception 响应写入失败时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 从请求头中获取Authorization令牌
        String token = request.getHeader("Authorization");

        log.info("请求路径: {}, Authorization头: {}", request.getRequestURI(), token);

        // 检查令牌是否存在且格式为Bearer开头
        if (token == null || !token.startsWith("Bearer ")) {
            log.warn("Token缺失或格式错误");
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":1005,\"message\":\"" + ResultCode.UNAUTHORIZED.getMessage() + "\"}");
            return false;
        }

        // 去除"Bearer "前缀，提取实际令牌
        token = token.substring(7);
        log.info("提取的token: {}", token);

        // 检查令牌是否已被加入黑名单（登出后失效）
        String blacklistKey = RedisConstants.TOKEN_BLACKLIST + token;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            log.warn("Token已被加入黑名单");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1005,\"message\":\"Token已失效\"}");
            return false;
        }

        // 验证令牌签名和有效期
        if (!jwtUtil.validateToken(token)) {
            log.warn("Token验证失败");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1005,\"message\":\"" + ResultCode.UNAUTHORIZED.getMessage() + "\"}");
            return false;
        }

        // 从令牌中解析用户信息
        Long userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        log.info("解析结果: userId={}, role={}", userId, role);

        // 确保解析到有效的userId
        if (userId == null) {
            log.warn("Token中未解析到userId");
            response.setStatus(401);
            response.getWriter().write("{\"code\":1005,\"message\":\"Token无效\"}");
            return false;
        }

        // 将用户信息注入请求属性，供后续处理器使用
        request.setAttribute("userId", userId);
        request.setAttribute("role", role);

        log.info("已设置userId到request: {}", request.getAttribute("userId"));

        return true;
    }
}