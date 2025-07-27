package com.lore.master.web.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(
                            "http://localhost:3000",   // 默认端口
                            "http://localhost:3001",   // 管理端
                            "http://localhost:3002",   // B端
                            "http://localhost:3003",   // C端
                            "http://localhost:3004"    // 小程序
                        )
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowCredentials(true)
                        .allowedHeaders("*", "Authorization", "Content-Type", "X-Requested-With")
                        .exposedHeaders("Authorization");
            }
        };
    }
} 