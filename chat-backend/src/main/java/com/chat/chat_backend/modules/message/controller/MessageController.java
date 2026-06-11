package com.chat.chat_backend.modules.message.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chat.chat_backend.common.constant.MessageConstants;
import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.common.utils.OssUtil;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import com.chat.chat_backend.modules.user.mapper.UserMapper;
import com.chat.chat_backend.modules.message.dto.response.MessageVO;
import com.chat.chat_backend.modules.message.dto.response.UnreadCountVO;
import com.chat.chat_backend.modules.user.entity.User;
import com.chat.chat_backend.modules.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 消息控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@RestController
@RequestMapping("/message")
@RequiredArgsConstructor
public class MessageController {

    /** 消息服务 */
    private final MessageService messageService;
    /** 用户 Mapper */
    private final UserMapper userMapper;
    /** OSS 工具类 */
    private final OssUtil ossUtil;

    /**
     * 获取聊天记录
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param friendId 好友 ID
     * @param page     页码
     * @param size     每页大小
     * @return 聊天记录分页结果
     */
    @GetMapping("/history/{friendId}")
    public Result<Page<MessageVO>> getChatHistory(HttpServletRequest request,
                                                  @PathVariable Long friendId,
                                                  @RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "20") Integer size) {
        Long userId = (Long) request.getAttribute("userId");
        Page<MessageVO> result = messageService.getChatHistory(userId, friendId, page, size);
        return Result.success(result);
    }

    /**
     * 下载聊天记录
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param response HTTP 响应对象
     * @param friendId 好友 ID
     * @param limit    下载条数上限
     * @throws Exception 下载异常
     */
    @GetMapping("/download/{friendId}")
    public void downloadChatHistory(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable Long friendId,
                                    @RequestParam(value = "limit", defaultValue = "100") Integer limit) throws Exception {
        Long userId = (Long) request.getAttribute("userId");

        log.info("下载聊天记录: userId={}, friendId={}, limit={}", userId, friendId, limit);

        if (limit > MessageConstants.MAX_DOWNLOAD_SIZE) {
            limit = MessageConstants.MAX_DOWNLOAD_SIZE;
        }
        if (limit <= 0) {
            limit = MessageConstants.DEFAULT_DOWNLOAD_SIZE;
        }

        List<MessageVO> messages = messageService.downloadChatHistory(userId, friendId, limit);

        if (messages == null || messages.isEmpty()) {
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("暂无聊天记录可导出");
            return;
        }

        User friend = userMapper.selectById(friendId);
        String friendName = friend != null ? friend.getNickname() : "好友";

        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Content-Disposition",
                "attachment;filename=" + URLEncoder.encode(friendName + "_聊天记录_" + System.currentTimeMillis() + ".txt", "UTF-8"));

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("聊天记录导出\n");
        sb.append("导出时间: ").append(new Date()).append("\n");
        sb.append("聊天对象: ").append(friendName).append("\n");
        sb.append("共 ").append(messages.size()).append(" 条消息\n");
        sb.append("========================================\n\n");

        for (MessageVO msg : messages) {
            sb.append("[").append(msg.getSendTime()).append("] ");
            sb.append(msg.getFromUserNickname()).append(": ");
            sb.append(msg.getContent()).append("\n");
        }

        response.getWriter().write(sb.toString());
    }

    /**
     * 标记消息已读
     *
     * @param request  HTTP 请求对象（包含用户信息）
     * @param friendId 好友 ID
     * @return 操作结果
     */
    @PutMapping("/read/{friendId}")
    public Result<Void> markAsRead(HttpServletRequest request, @PathVariable Long friendId) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.markAsRead(userId, friendId);
        return Result.success("已标记已读", null);
    }

    /**
     * 获取未读消息统计
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 未读消息统计
     */
    @GetMapping("/unread/count")
    public Result<UnreadCountVO> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        UnreadCountVO result = messageService.getUnreadCount(userId);
        return Result.success(result);
    }

    /**
     * 搜索文本消息
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param keyword 搜索关键词
     * @param limit   结果数量上限（默认20）
     * @return 搜索结果列表
     */
    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchMessages(HttpServletRequest request,
                                                             @RequestParam String keyword,
                                                             @RequestParam(defaultValue = "20") Integer limit) {
        Long userId = (Long) request.getAttribute("userId");
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(List.of());
        }
        List<Map<String, Object>> result = messageService.searchMessages(userId, keyword.trim(), limit);
        return Result.success(result);
    }

    /**
     * 撤回消息
     *
     * @param request   HTTP 请求对象（包含用户信息）
     * @param messageId 消息 ID
     * @return 操作结果
     */
    @PutMapping("/recall/{messageId}")
    public Result<Void> recallMessage(HttpServletRequest request, @PathVariable Long messageId) {
        Long userId = (Long) request.getAttribute("userId");
        messageService.recallMessage(userId, messageId);
        return Result.success("撤回成功", null);
    }

    /**
     * 上传图片
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param file    图片文件
     * @return 图片访问 URL
     */
    @PostMapping("/upload/image")
    public Result<String> uploadImage(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String url = ossUtil.uploadFile(file, "chat/images/");
            log.info("图片上传成功: userId={}, url={}", userId, url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传图片失败", e);
            return Result.error("上传失败");
        }
    }

    /**
     * 上传语音
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param file    语音文件
     * @return 语音访问 URL
     */
    @PostMapping("/upload/voice")
    public Result<String> uploadVoice(HttpServletRequest request, @RequestParam("file") MultipartFile file) {
        Long userId = (Long) request.getAttribute("userId");
        try {
            String url = ossUtil.uploadFile(file, "chat/voice/");
            log.info("语音上传成功: userId={}, url={}", userId, url);
            return Result.success(url);
        } catch (Exception e) {
            log.error("上传语音失败", e);
            return Result.error("上传失败");
        }
    }

    /**
     * 代理音频文件（解决跨域）
     *
     * @param url      音频文件 URL
     * @param response HTTP 响应对象
     */
    @GetMapping("/proxy-audio")
    public void proxyAudio(@RequestParam String url, HttpServletResponse response) {
        try {
            URL ossUrl = URI.create(url).toURL();
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) ossUrl.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.connect();

            String contentType = conn.getContentType();
            if (contentType != null) response.setContentType(contentType);
            else response.setContentType("audio/webm");

            response.setHeader("Access-Control-Allow-Origin", "*");
            response.setHeader("Cache-Control", "public, max-age=86400");

            try (InputStream is = conn.getInputStream()) {
                is.transferTo(response.getOutputStream());
                response.flushBuffer();
            }
            conn.disconnect();
        } catch (Exception e) {
            log.error("代理音频失败: {}", url, e);
            response.setStatus(500);
        }
    }
}