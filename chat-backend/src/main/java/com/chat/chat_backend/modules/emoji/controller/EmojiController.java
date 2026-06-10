package com.chat.chat_backend.modules.emoji.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import com.chat.chat_backend.modules.emoji.service.EmojiService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 表情接口控制器，负责系统表情、自定义表情的查询、上传和删除。
 */
@RestController
@RequestMapping("/emoji")
@RequiredArgsConstructor
public class EmojiController {

    private final EmojiService emojiService;

    /**
     * 查询系统内置表情。
     */
    @GetMapping("/system")
    public Result<List<EmojiVO>> listSystemEmojis() {
        List<EmojiVO> emojis = emojiService.getSystemEmojis();
        return Result.success(emojis);
    }

    /**
     * 查询当前登录用户上传的自定义表情。
     */
    @GetMapping("/user")
    public Result<List<EmojiVO>> listUserEmojis(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        List<EmojiVO> emojis = emojiService.getUserEmojis(userId);
        return Result.success(emojis);
    }

    /**
     * 上传当前登录用户的自定义表情。
     */
    @PostMapping("/upload")
    public Result<EmojiVO> uploadEmoji(
            HttpServletRequest request,
            @RequestParam("file") MultipartFile file,
            @RequestParam("name") String name,
            @RequestParam(value = "category", required = false) String category
    ) {
        Long userId = getCurrentUserId(request);
        EmojiVO emoji = emojiService.uploadEmoji(userId, name, file, category);
        return Result.success(emoji);
    }

    /**
     * 删除当前登录用户的自定义表情。
     */
    @DeleteMapping("/{emojiId}")
    public Result<Void> deleteEmoji(HttpServletRequest request, @PathVariable Long emojiId) {
        Long userId = getCurrentUserId(request);
        emojiService.deleteEmoji(userId, emojiId);
        return Result.success("删除成功", null);
    }

    private Long getCurrentUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }
}
