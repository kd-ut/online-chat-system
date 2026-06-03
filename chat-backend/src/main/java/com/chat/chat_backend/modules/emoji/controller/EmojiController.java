package com.chat.chat_backend.modules.emoji.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.emoji.dto.response.EmojiVO;
import com.chat.chat_backend.modules.emoji.service.EmojiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 表情控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@RestController
@RequestMapping("/emoji")
@RequiredArgsConstructor
public class EmojiController {

    /** 表情服务 */
    private final EmojiService emojiService;

    /**
     * 获取系统表情列表
     *
     * @return 系统表情列表
     */
    @GetMapping("/system")
    public Result<List<EmojiVO>> getSystemEmojis() {
        return Result.success(emojiService.getSystemEmojis());
    }

    /**
     * 获取用户自定义表情列表
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 用户自定义表情列表
     */
    @GetMapping("/user")
    public Result<List<EmojiVO>> getUserEmojis(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(emojiService.getUserEmojis(userId));
    }

    /**
     * 上传自定义表情
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param file     表情文件
     * @param name     表情名称
     * @param category 表情分类（可选）
     * @return 上传后的表情信息
     */
    @PostMapping("/upload")
    public Result<EmojiVO> uploadEmoji(HttpServletRequest request,
                                       @RequestParam("file") MultipartFile file,
                                       @RequestParam("name") String name,
                                       @RequestParam(value = "category", required = false) String category) {
        Long userId = (Long) request.getAttribute("userId");
        EmojiVO result = emojiService.uploadEmoji(userId, name, file, category);
        return Result.success(result);
    }

    /**
     * 删除自定义表情
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param emojiId 表情 ID
     * @return 操作结果
     */
    @DeleteMapping("/{emojiId}")
    public Result<Void> deleteEmoji(HttpServletRequest request, @PathVariable Long emojiId) {
        Long userId = (Long) request.getAttribute("userId");
        emojiService.deleteEmoji(userId, emojiId);
        return Result.success("删除成功", null);
    }
}