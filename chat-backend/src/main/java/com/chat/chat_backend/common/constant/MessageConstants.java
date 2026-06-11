package com.chat.chat_backend.common.constant;

/**
 * 消息常量定义，包含消息类型、消息状态、限制参数和聊天记录下载设置
 * @author chat-backend
 * @since 2026-05-12
 */
public class MessageConstants {

    // ========== 消息类型 ==========
    /** 文本消息 */
    public static final int MSG_TYPE_TEXT = 1;
    /** 图片消息 */
    public static final int MSG_TYPE_IMAGE = 2;
    /** 文件消息 */
    public static final int MSG_TYPE_FILE = 3;
    /** 语音消息 */
    public static final int MSG_TYPE_VOICE = 4;
    /** 语音通话记录 */
    public static final int MSG_TYPE_VOICE_CALL = 5;
    /** 视频通话记录 */
    public static final int MSG_TYPE_VIDEO_CHAT = 6;

    // ========== 消息状态 ==========
    /** 消息未读 */
    public static final int MSG_UNREAD = 0;
    /** 消息已读 */
    public static final int MSG_READ = 1;

    // ========== 消息限制 ==========
    /** 消息最大内容长度 */
    public static final int MAX_CONTENT_LENGTH = 500;
    /** 分页大小 */
    public static final int PAGE_SIZE = 20;
    /** 消息可撤回时间限制（分钟） */
    public static final int RECALL_TIME_LIMIT_MINUTES = 2;

    // ========== 聊天记录下载 ==========
    /** 最大下载条数 */
    public static final int MAX_DOWNLOAD_SIZE = 500;
    /** 默认下载条数 */
    public static final int DEFAULT_DOWNLOAD_SIZE = 100;

    /** 下载文件前缀 */
    public static final String DOWNLOAD_FILE_PREFIX = "chat_";
    /** 下载文件扩展名 */
    public static final String DOWNLOAD_FILE_EXTENSION = ".txt";
}