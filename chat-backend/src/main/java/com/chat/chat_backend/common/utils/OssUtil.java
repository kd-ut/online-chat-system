package com.chat.chat_backend.common.utils;

import com.aliyun.oss.ClientBuilderConfiguration;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.ObjectMetadata;
import com.chat.chat_backend.config.AliyunOSSProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 阿里云OSS文件上传工具类，支持V4签名、按日期分目录存储和UUID文件名
 * @author chat-backend
 * @since 2026-05-12
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OssUtil {

    /** 阿里云OSS配置属性 */
    private final AliyunOSSProperties aliyunOSSProperties;

    /** 本地文件存储根目录 */
    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    /** 是否使用OSS（配置完整时自动启用） */
    private boolean ossEnabled = false;

    @PostConstruct
    public void init() {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        ossEnabled = endpoint != null && !endpoint.isEmpty()
                && accessKeyId != null && !accessKeyId.isEmpty();
        if (ossEnabled) {
            log.info("OSS已配置，将使用阿里云OSS存储文件");
        } else {
            log.info("OSS未配置，将使用本地文件存储: {}", uploadDir);
            try {
                Files.createDirectories(Paths.get(uploadDir));
            } catch (IOException e) {
                log.warn("无法创建上传目录: {}", e.getMessage());
            }
        }
    }

    /**
     * 上传文件到OSS指定目录，使用自动生成的文件名
     * @param file 待上传的文件
     * @param folder 目标目录前缀（如"avatars/"）
     * @return 上传文件的公开访问URL
     * @throws IOException 文件读取失败时抛出
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        return uploadFile(file, folder, null);
    }

    /**
     * 上传文件到OSS指定目录或本地文件系统，使用UUID文件名
     * @param file 待上传的文件
     * @param folder 目标目录前缀
     * @param oldFileUrl 旧文件URL（当前未使用，预留参数）
     * @return 上传文件的公开访问URL
     * @throws IOException 文件读取失败时抛出
     */
    public String uploadFile(MultipartFile file, String folder, String oldFileUrl) throws IOException {
        if (ossEnabled) {
            return uploadToOss(file, folder);
        }
        return uploadToLocal(file, folder);
    }

    /**
     * 上传到阿里云OSS
     */
    private String uploadToOss(MultipartFile file, String folder) throws IOException {
        String endpoint = aliyunOSSProperties.getEndpoint();
        String bucketName = aliyunOSSProperties.getBucketName();
        String region = aliyunOSSProperties.getRegion();
        String accessKeyId = aliyunOSSProperties.getAccessKeyId();
        String accessKeySecret = aliyunOSSProperties.getAccessKeySecret();

        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);

        String dir = folder + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFileName = UUID.randomUUID() + extension;
        String objectName = dir + "/" + newFileName;

        ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
        clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

        OSS ossClient = OSSClientBuilder.create()
                .endpoint(endpoint)
                .credentialsProvider(credentialsProvider)
                .clientConfiguration(clientBuilderConfiguration)
                .region(region)
                .build();

        try {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentType(file.getContentType());
            ossClient.putObject(bucketName, objectName, file.getInputStream(), meta);
            log.info("OSS上传成功: {}, contentType={}", objectName, file.getContentType());
            return "https://" + bucketName + "." + endpoint.replace("https://", "") + "/" + objectName;
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 上传到本地文件系统
     */
    private String uploadToLocal(MultipartFile file, String folder) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFileName = UUID.randomUUID() + extension;

        // 按日期分目录：folder/yyyy/MM
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        Path targetDir = Paths.get(uploadDir, folder, datePath);
        Files.createDirectories(targetDir);

        Path targetFile = targetDir.resolve(newFileName);
        Files.copy(file.getInputStream(), targetFile);

        String urlPath = "/uploads/" + folder + datePath + "/" + newFileName;
        log.info("本地存储成功: {}", urlPath);
        return urlPath;
    }
}