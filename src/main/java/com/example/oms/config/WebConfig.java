package com.example.oms.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 
 * 配置跨域（CORS）、静态资源映射等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置跨域资源共享（CORS）
     * 允许前端页面（即使是不同域名）调用后端 API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")              // 允许 /api/** 路径跨域
                .allowedOriginPatterns("*")         // 允许所有来源
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")                // 允许所有请求头
                .allowCredentials(true)             // 允许携带凭证（Cookie）
                .maxAge(3600);                      // 预检请求缓存时间（秒）
    }
}
