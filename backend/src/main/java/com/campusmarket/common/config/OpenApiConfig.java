package com.campusmarket.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI 配置 — Swagger UI 自动生成 API 文档
 *
 * 访问地址：http://localhost:8080/api/swagger-ui.html
 * JSON 格式：http://localhost:8080/api/v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI campusMarketOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Campus Market API")
                        .description("校园二手交易平台 — RESTful API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Campus Market")
                                .url("http://localhost:5173")));
    }
}
