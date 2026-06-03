package com.chat.chat_backend.config;

import com.chat.chat_backend.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类，注册JWT拦截器、CORS跨域设置和静态资源映射
 * @author chat-backend
 * @since 2026-05-12
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /** JWT认证拦截器，用于请求鉴权 */
    private final JwtInterceptor jwtInterceptor;

    /** 本地文件上传根目录 */
    @Value("${app.upload.dir:${user.dir}/uploads}")
    private String uploadDir;

    /**
     * 注册拦截器，拦截所有请求，排除注册、登录、健康检查、WebSocket和上传资源路径
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/user/register",
                        "/user/login",
                        "/api/health",
                        "/actuator/**",
                        "/ws/**",
                        "/uploads/**"
                );
    }

    /**
     * 配置静态资源映射，用于访问本地存储的上传文件
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }

    /**
     * 配置CORS跨域，允许所有来源、方法和请求头，支持凭证
     * @param registry CORS注册表
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}