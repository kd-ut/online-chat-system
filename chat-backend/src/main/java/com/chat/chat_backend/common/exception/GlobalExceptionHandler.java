package com.chat.chat_backend.common.exception;

import com.chat.chat_backend.common.result.Result;
import com.chat.chat_backend.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，捕获业务异常、参数异常和系统异常并转换为统一API响应
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常，返回对应的错误码和消息
     * @param e 捕获的BusinessException
     * @return 标准化错误结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数验证失败的非法参数异常
     * @param e 捕获的IllegalArgumentException
     * @return 标准化错误结果（400状态码）
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(ResultCode.PARAM_ERROR.getCode(), e.getMessage());
    }

    /**
     * 处理所有未捕获的系统异常（兜底处理）
     * @param e 捕获的Exception
     * @return 标准化错误结果（500状态码）
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常: ", e);
        return Result.error(ResultCode.ERROR);
    }
}