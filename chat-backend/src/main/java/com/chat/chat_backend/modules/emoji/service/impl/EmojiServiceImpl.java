package com.chat.chat_backend.modules.emoji.service.impl;

import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.OssUtil;
import com.chat.chat_backend.modules.emoji.mapper.EmojiMapper;
import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import com.chat.chat_backend.modules.emoji.entity.Emoji;
import com.chat.chat_backend.modules.emoji.service.EmojiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** 表情服务实现，处理系统表情查询、自定义表情上传/删除等业务逻辑 @author chat-backend @since 2026-05-12 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmojiServiceImpl implements EmojiService {

    /** 表情数据访问层 */
    private final EmojiMapper emojiMapper;
    /** OSS对象存储工具类 */
    private final OssUtil ossUtil;

    /** 获取系统表情列表 @return 系统表情列表 */
    @Override
    public List<EmojiVO> getSystemEmojis() {
        return emojiMapper.findSystemEmojis().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /** 获取用户自定义表情列表 @param userId 用户ID @return 用户表情列表 */
    @Override
    public List<EmojiVO> getUserEmojis(Long userId) {
        return emojiMapper.findUserEmojis(userId).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    /** 上传自定义表情到OSS @param userId 用户ID @param name 表情名称 @param file 图片文件 @param category 表情分类 @return 上传后的表情信息 */
    @Override
    @Transactional
    public EmojiVO uploadEmoji(Long userId, String name, MultipartFile file, String category) {
        try {
            String url = ossUtil.uploadFile(file, "emoji/");

            Emoji emoji = new Emoji();
            emoji.setName(name);
            emoji.setUrl(url);
            emoji.setCategory(category != null ? category : "custom");
            emoji.setUserId(userId);
            emoji.setIsSystem(0);
            emoji.setCreatedAt(LocalDateTime.now());
            emojiMapper.insert(emoji);

            log.info("用户 {} 上传了表情: {}", userId, name);
            return toVO(emoji);
        } catch (Exception e) {
            log.error("上传表情失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "上传失败");
        }
    }

    /** 删除自定义表情（仅表情拥有者可操作） @param userId 用户ID @param emojiId 表情ID */
    @Override
    @Transactional
    public void deleteEmoji(Long userId, Long emojiId) {
        Emoji emoji = emojiMapper.selectById(emojiId);
        if (emoji == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "表情不存在");
        }
        if (!emoji.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        emojiMapper.deleteById(emojiId);
        log.info("用户 {} 删除了表情: {}", userId, emoji.getName());
    }

    /** 表情实体转VO @param emoji 表情实体 @return 表情VO */
    private EmojiVO toVO(Emoji emoji) {
        return EmojiVO.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .url(emoji.getUrl())
                .category(emoji.getCategory())
                .userId(emoji.getUserId())
                .isSystem(emoji.getIsSystem() == 1)
                .createdAt(emoji.getCreatedAt())
                .build();
    }
}