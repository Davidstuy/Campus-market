package com.campusmarket.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置：允许前端开发服务器 (localhost:5173) 访问后端
 */
@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("http://localhost:5173");
        config.addAllowedOriginPattern("http://localhost:5174");
        config.setAllowCredentials(true);           // 允许携带 Cookie/Token
        config.addAllowedMethod("*");               // 允许所有 HTTP 方法
        config.addAllowedHeader("*");               // 允许所有请求头

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);  // 对所有路径生效
        return new CorsFilter(source);
    }
}
