package com.plands.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:8082") // 클라이언트의 주소를 여기에 추가
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 필요한 HTTP 메서드만 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 쿠키를 허용하려면 true로 설정
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/profile/**")
                .addResourceLocations("file:/Users/imheejeong/project/plands/uploads/profile/");
        // 다른 이미지도 있다면 별도 등록 가능
        registry.addResourceHandler("/uploads/post/**")
                .addResourceLocations("file:/Users/imheejeong/project/plands/uploads/post/");

        // 한 줄 일기 이미지
        registry.addResourceHandler("/uploads/diary/**")
                .addResourceLocations("file:C:/plands/uploads/diary/");
    }
}
