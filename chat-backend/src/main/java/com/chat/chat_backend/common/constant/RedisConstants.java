package com.chat.chat_backend.common.constant;

/**
 * Redis键和过期时间常量，用于在线用户、未读计数和Token黑名单
 * @author chat-backend
 * @since 2026-05-12
 */
public class RedisConstants {
    /** 在线用户集合键 */
    public static final String ONLINE_USERS = "online:users";

    /** 未读消息计数前缀 */
    public static final String UNREAD_COUNT = "unread:";

    /** Token黑名单前缀 */
    public static final String TOKEN_BLACKLIST = "blacklist:token:";

    /** 在线状态过期时间（秒），默认30分钟 */
    public static final long ONLINE_EXPIRE_SECONDS = 1800;
    /** Token黑名单过期时间（秒），默认24小时 */
    public static final long TOKEN_BLACKLIST_EXPIRE_SECONDS = 86400;
}