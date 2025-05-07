package com.wappenable.be.users.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.wappenable.be.users.handler.OAuth2LoginFailureHandler;
import com.wappenable.be.users.handler.OAuth2LoginSuccessHandler;
import com.wappenable.be.users.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
public class SecurityConfig {
    
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;

    @Bean
    @Primary 
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // csrf.disable() -> 이거 나중에 지워야하나?
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/users/signup","/api/users/login",
                "/login/**", "/oauth2/**", "/login/oauth2/**").permitAll() // 누구나 접근 가능
                .anyRequest().authenticated() // 나머지는 인증 요구, 권한 없으면 접근 불가
            )
            .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> 
                    userInfo.userService(customOAuth2UserService)
                )
                .successHandler(oAuth2LoginSuccessHandler) // 성공 핸들러 등록
                .failureHandler(oAuth2LoginFailureHandler) // 실패 핸들러 등록
            )
            .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                // 인증 실패 (401)
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"인증이 필요합니다\"}");
            })
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                // 권한 부족 (403)
                response.setStatus(HttpStatus.FORBIDDEN.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"접근 권한이 없습니다\"}");
            })
        );
        return http.build();
    }
}