package com.chat.chat_backend.modules.impression.controller;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.modules.impression.dto.request.AddImpressionRequest;
import com.chat.chat_backend.modules.impression.dto.response.ImpressionVO;
import com.chat.chat_backend.modules.impression.service.ImpressionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 用户印象/评价控制器
 *
 * @author chat-backend
 * @since 2026-05-12
 */
@RestController
@RequestMapping("/impression")
@RequiredArgsConstructor
public class ImpressionController {

    /** 印象/评价服务 */
    private final ImpressionService impressionService;

    /**
     * 添加评价
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @param req     添加评价请求
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> addImpression(HttpServletRequest request, @RequestBody AddImpressionRequest req) {
        Long userId = (Long) request.getAttribute("userId");
        impressionService.addImpression(userId, req);
        return Result.success("评价成功", null);
    }

    /**
     * 获取对我的评价
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 评价列表
     */
    @GetMapping("/to-me")
    public Result<List<ImpressionVO>> getImpressionsToMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ImpressionVO> result = impressionService.getImpressionsToMe(userId);
        return Result.success(result);
    }

    /**
     * 获取我给出的评价
     *
     * @param request HTTP 请求对象（包含用户信息）
     * @return 评价列表
     */
    @GetMapping("/by-me")
    public Result<List<ImpressionVO>> getImpressionsByMe(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        List<ImpressionVO> result = impressionService.getImpressionsByMe(userId);
        return Result.success(result);
    }

    /**
     * 删除评价
     *
     * @param request       HTTP 请求对象（包含用户信息）
     * @param impressionId 评价 ID
     * @return 操作结果
     */
    @DeleteMapping("/{impressionId}")
    public Result<Void> deleteImpression(HttpServletRequest request, @PathVariable Long impressionId) {
        Long userId = (Long) request.getAttribute("userId");
        impressionService.deleteImpression(userId, impressionId);
        return Result.success("删除成功", null);
    }
}