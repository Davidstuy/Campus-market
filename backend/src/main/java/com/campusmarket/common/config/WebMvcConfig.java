package com.campusmarket.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置：映射本地文件路径为 URL，让前端能访问上传的图片
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /api/v1/files/** 映射到本地 ./uploads 目录
        registry.addResourceHandler("/v1/files/**")
                .addResourceLocations("file:./uploads/");
    }
}
