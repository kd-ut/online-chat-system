package com.chat.chat_backend.modules.emoji.service;

import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 表情服务接口，提供系统表情查询、用户自定义表情管理能力。
 */
public interface EmojiService {

    /**
     * 获取系统内置表情列表。
     *
     * @return 系统表情列表
     */
    List<EmojiVO> getSystemEmojis();

    /**
     * 获取指定用户的自定义表情列表。
     *
     * @param userId 用户 ID
     * @return 用户自定义表情列表
     */
    List<EmojiVO> getUserEmojis(Long userId);

    /**
     * 上传用户自定义表情。
     *
     * @param userId   用户 ID
     * @param name     表情名称
     * @param file     表情图片文件
     * @param category 表情分类，可为空
     * @return 上传后的表情信息
     */
    EmojiVO uploadEmoji(Long userId, String name, MultipartFile file, String category);

    /**
     * 删除用户自定义表情。
     *
     * @param userId  用户 ID
     * @param emojiId 表情 ID
     */
    void deleteEmoji(Long userId, Long emojiId);
}
