package com.campus.lostfound.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j 接口文档配置
 */
@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园失物招领系统 API")
                        .description("基于多模态AI的校园失物招领微信小程序 - 后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发者")
                                .email("student@campus.edu")));
    }
}
