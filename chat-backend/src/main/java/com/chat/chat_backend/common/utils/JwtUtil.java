package com.chat.chat_backend.common.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于生成、解析和验证JSON Web令牌
 * 使用HMAC-SHA签名，支持可配置的密钥和过期时间
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
public class JwtUtil {

    /** HMAC签名密钥，从配置中加载 */
    @Value("${jwt.secret:aB3dE5fG7hI9jK1lM2nO4pQ6rS8tU0vW2xY4zA6cC8eE0gG2iI4kK6mM8oO0qQ}")
    private String secret;

    /** Token过期时间（毫秒） */
    @Value("${jwt.expiration:86400000}")
    private Long expiration;

    /**
     * 根据配置的密钥字符串构建HMAC-SHA签名密钥
     * @return JWT签名密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成包含userId、username和role声明的JWT令牌
     * @param userId 用户ID
     * @param username 用户名
     * @param role 用户角色
     * @return 签名后的JWT令牌字符串
     * @throws IllegalArgumentException 如果userId为null
     */
    public String generateToken(Long userId, String username, String role) {
        if (userId == null) {
            log.error("生成Token失败: userId为null");
            throw new IllegalArgumentException("userId不能为null");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 从JWT令牌中提取userId声明，支持Long、Integer和String类型
     * @param token JWT令牌
     * @return userId（存在且有效时），否则返回null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) return null;
        Object userIdObj = claims.get("userId");
        if (userIdObj == null) return null;
        if (userIdObj instanceof Long) return (Long) userIdObj;
        if (userIdObj instanceof Integer) return ((Integer) userIdObj).longValue();
        if (userIdObj instanceof String) return Long.parseLong((String) userIdObj);
        return null;
    }

    /**
     * 从JWT令牌中提取用户名声明
     * @param token JWT令牌
     * @return 用户名（存在时），否则返回null
     */
    public String getUsernameFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("username") : null;
    }

    /**
     * 从JWT令牌中提取角色声明
     * @param token JWT令牌
     * @return 角色（存在时），否则返回null
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * 验证JWT令牌的结构有效性和是否过期
     * @param token JWT令牌
     * @return 如果有效且未过期返回true，否则返回false
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !claims.getExpiration().before(new Date());
        } catch (Exception e) {
            log.warn("Token验证失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 解析JWT令牌并返回声明体，解析或验证失败时返回null
     * @param token JWT令牌
     * @return 声明体（如果有效），否则返回null
     */
    private Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.warn("Token解析失败: {}", e.getMessage());
            return null;
        }
    }
}