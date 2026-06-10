package com.chat.chat_backend.modules.emoji.service.impl;

import com.chat.chat_backend.common.exception.BusinessException;
import com.chat.chat_backend.common.result.ResultCode;
import com.chat.chat_backend.common.utils.OssUtil;
import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import com.chat.chat_backend.modules.emoji.entity.Emoji;
import com.chat.chat_backend.modules.emoji.mapper.EmojiMapper;
import com.chat.chat_backend.modules.emoji.service.EmojiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 表情服务实现，处理系统表情查询和用户自定义表情的上传、删除。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmojiServiceImpl implements EmojiService {

    private static final String DEFAULT_CATEGORY = "custom";
    private static final String EMOJI_UPLOAD_DIR = "emoji/";

    private final EmojiMapper emojiMapper;
    private final OssUtil ossUtil;

    @Override
    public List<EmojiVO> getSystemEmojis() {
        return emojiMapper.findSystemEmojis().stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public List<EmojiVO> getUserEmojis(Long userId) {
        return emojiMapper.findUserEmojis(userId).stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional
    public EmojiVO uploadEmoji(Long userId, String name, MultipartFile file, String category) {
        try {
            String url = ossUtil.uploadFile(file, EMOJI_UPLOAD_DIR);
            Emoji emoji = buildCustomEmoji(userId, name, url, category);

            emojiMapper.insert(emoji);
            log.info("用户 {} 上传了表情: {}", userId, name);
            return toVO(emoji);
        } catch (Exception e) {
            log.error("上传表情失败", e);
            throw new BusinessException(ResultCode.ERROR.getCode(), "上传失败");
        }
    }

    @Override
    @Transactional
    public void deleteEmoji(Long userId, Long emojiId) {
        Emoji emoji = findEmojiOrThrow(emojiId);
        checkEmojiOwner(emoji, userId);

        emojiMapper.deleteById(emojiId);
        log.info("用户 {} 删除了表情: {}", userId, emoji.getName());
    }

    private Emoji buildCustomEmoji(Long userId, String name, String url, String category) {
        Emoji emoji = new Emoji();
        emoji.setName(name);
        emoji.setUrl(url);
        emoji.setCategory(resolveCategory(category));
        emoji.setUserId(userId);
        emoji.setIsSystem(0);
        emoji.setCreatedAt(LocalDateTime.now());
        return emoji;
    }

    private String resolveCategory(String category) {
        return category == null || category.isBlank() ? DEFAULT_CATEGORY : category;
    }

    private Emoji findEmojiOrThrow(Long emojiId) {
        Emoji emoji = emojiMapper.selectById(emojiId);
        if (emoji == null) {
            throw new BusinessException(ResultCode.ERROR.getCode(), "表情不存在");
        }
        return emoji;
    }

    private void checkEmojiOwner(Emoji emoji, Long userId) {
        if (!Objects.equals(emoji.getUserId(), userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    private EmojiVO toVO(Emoji emoji) {
        return EmojiVO.builder()
                .id(emoji.getId())
                .name(emoji.getName())
                .url(emoji.getUrl())
                .category(emoji.getCategory())
                .userId(emoji.getUserId())
                .isSystem(Objects.equals(emoji.getIsSystem(), 1))
                .createdAt(emoji.getCreatedAt())
                .build();
    }
}
