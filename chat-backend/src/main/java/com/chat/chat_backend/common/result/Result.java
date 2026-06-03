package com.chat.chat_backend.common.result;

import lombok.Data;

/**
 * 统一API响应封装，提供静态工厂方法用于构建成功/失败结果
 * @param <T> 响应数据类型
 * @author chat-backend
 * @since 2026-05-12
 */
@Data
public class Result<T> {

    /** 响应状态码 */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 私有构造方法
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     */
    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 返回无数据的成功结果
     * @param <T> 数据类型
     * @return 成功结果（data为null）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 返回带数据的成功结果
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 返回自定义消息和数据的成功结果
     * @param message 自定义成功消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功结果
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 使用预定义错误码返回失败结果
     * @param resultCode 预定义结果码枚举
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 使用自定义消息返回失败结果（默认500错误码）
     * @param message 自定义错误消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 使用自定义错误码和消息返回失败结果
     * @param code 自定义错误码
     * @param message 自定义错误消息
     * @param <T> 数据类型
     * @return 失败结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }
}