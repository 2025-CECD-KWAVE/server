package com.example.kwave.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173", "http://13.125.226.149", "https://2025-cecd-kwave-client.vercel.app/")
                .allowedMethods("GET", "POST", "OPTIONS")
                .exposedHeaders("Authorization")
                .allowCredentials(true);  // 쿠키 포함 허용 시
    }
}