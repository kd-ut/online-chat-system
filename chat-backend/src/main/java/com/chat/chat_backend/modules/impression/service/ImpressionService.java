package com.chat.chat_backend.modules.impression.service;

import com.chat.chat_backend.modules.impression.dto.request.AddImpressionRequest;
import com.chat.chat_backend.modules.impression.dto.response.ImpressionVO;
import java.util.List;

/** 用户评价服务接口，提供添加评价、查询收到的/发出的评价、删除评价等功能 @author chat-backend @since 2026-05-12 */
public interface ImpressionService {

    /** 添加用户评价（自动生成系统通知消息） @param userId 评价者用户ID @param request 评价请求（目标用户ID、评价内容） */
    void addImpression(Long userId, AddImpressionRequest request);

    /** 获取收到的评价列表 @param userId 用户ID @return 评价列表 */
    List<ImpressionVO> getImpressionsToMe(Long userId);

    /** 获取发出的评价列表 @param userId 用户ID @return 评价列表 */
    List<ImpressionVO> getImpressionsByMe(Long userId);

    /** 删除评价（软删除） @param userId 用户ID @param impressionId 评价ID */
    void deleteImpression(Long userId, Long impressionId);
}