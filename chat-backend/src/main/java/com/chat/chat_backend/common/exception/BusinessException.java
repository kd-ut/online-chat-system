package com.chat.chat_backend.common.exception;

import com.chat.chat_backend.common.result.ResultCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 自定义业务异常，携带错误码和错误消息用于结构化异常处理
 * @author chat-backend
 * @since 2026-05-12
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private Integer code;

    /** 业务错误描述 */
    private String message;

    /**
     * 根据枚举结果码构造业务异常
     * @param resultCode 预定义结果码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    /**
     * 根据指定错误码和消息构造业务异常
     * @param code 业务错误码
     * @param message 错误描述
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 根据错误消息构造业务异常，默认错误码为500
     * @param message 错误描述
     */
    public BusinessException(String message) {
        super(message);
        this.code = 500;
        this.message = message;
    }
}