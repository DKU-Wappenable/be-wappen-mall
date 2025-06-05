package com.wappenable.be.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // /uploads/** 경로로 접근한 파일은 프로젝트 루트의 uploads 폴더에서 찾도록 설정
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
