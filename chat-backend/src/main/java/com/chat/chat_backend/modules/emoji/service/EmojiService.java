package com.chat.chat_backend.modules.emoji.service;

import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

/** 表情服务接口，提供系统表情查询、自定义表情上传/删除等功能 @author chat-backend @since 2026-05-12 */
public interface EmojiService {

    /** 获取系统表情列表 @return 系统表情列表 */
    List<EmojiVO> getSystemEmojis();

    /** 获取用户自定义表情列表 @param userId 用户ID @return 用户表情列表 */
    List<EmojiVO> getUserEmojis(Long userId);

    /** 上传自定义表情 @param userId 用户ID @param name 表情名称 @param file 图片文件 @param category 表情分类 @return 上传后的表情信息 */
    EmojiVO uploadEmoji(Long userId, String name, MultipartFile file, String category);

    /** 删除自定义表情 @param userId 用户ID @param emojiId 表情ID */
    void deleteEmoji(Long userId, Long emojiId);
}