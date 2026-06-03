package com.chat.chat_backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置属性类，绑定"aliyun.oss"前缀的配置项
 * @author chat-backend
 * @since 2026-05-12
 */
@Data
@Component
@ConfigurationProperties(prefix = "aliyun.oss")
public class AliyunOSSProperties {
    /** OSS访问端点URL */
    private String endpoint;
    /** OSS存储空间名称 */
    private String bucketName;
    /** OSS地域ID（如cn-beijing） */
    private String region;
    /** 阿里云访问密钥ID */
    private String accessKeyId;
    /** 阿里云访问密钥密码 */
    private String accessKeySecret;
}