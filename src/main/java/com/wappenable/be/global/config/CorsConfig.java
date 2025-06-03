package com.wappenable.be.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * CORS(Cross-Origin Resource Sharing) 설정 클래스
 * 프론트엔드와 백엔드 간의 교차 출처 리소스 공유를 허용
 */
@Configuration
public class CorsConfig {
    
    /**
     * CORS 설정을 위한 CorsConfigurationSource Bean
     * @Primary로 우선순위 지정 - mvcHandlerMappingIntrospector와의 빈 충돌 방지
     */
    @Bean
    @Primary
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 허용할 Origin 설정 (프론트엔드 개발 서버)
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite 기본 포트
                "http://localhost:3000",  // React 기본 포트 (백업)
                "http://127.0.0.1:5173", // 로컬호스트 별칭
                "http://127.0.0.1:3000"  // 로컬호스트 별칭 (백업)
        )); 
        
        // 허용할 HTTP Method 설정
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        )); 
        
        // 허용할 Header 설정 (모든 헤더 허용)
        config.setAllowedHeaders(Arrays.asList("*")); 
        
        // 인증정보(쿠키, Authorization 헤더 등) 허용 여부
        config.setAllowCredentials(true); 
        
        // 클라이언트에 노출할 헤더 설정
        config.setExposedHeaders(Arrays.asList("Authorization", "Content-Type")); 
        
        // preflight 요청 캐시 시간 설정 (1시간)
        config.setMaxAge(3600L);

        // URL 기반 CORS 설정 소스 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config); // 모든 경로에 적용
        
        return source;
    }
}
