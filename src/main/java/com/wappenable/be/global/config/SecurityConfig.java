package com.wappenable.be.global.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.wappenable.be.global.security.jwt.JwtAuthenticationFilter;
import com.wappenable.be.global.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.wappenable.be.global.security.oauth2.handler.OAuth2LoginSuccessHandler;
import com.wappenable.be.global.security.oauth2.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApplicationContext context;
    
    @Bean
    @Primary 
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // csrf.disable() -> 이거 나중에 지워야하나?
            // 현재 인증 방식 : JWT, 세션 저장이 필요 없는데 Spring Security는 기본적으로 세션에 인증 정보를 자동 저장하려고 시도함
            .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/api/users/signup","/api/users/login",
                "/login/**", "/oauth2/**", "/login/oauth2/**", "/error").permitAll() // 누구나 접근 가능
                .anyRequest().authenticated() // 나머지는 인증 요구, 권한 없으면 접근 불가
            )
            // .oauth2Login(oauth2 -> oauth2
            //     .userInfoEndpoint(userInfo -> 
            //         userInfo.userService(customOAuth2UserService)
            //     )
            //     .successHandler(oAuth2LoginSuccessHandler) // 성공 핸들러 등록
            //     .failureHandler(oAuth2LoginFailureHandler) // 실패 핸들러 등록
            // )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    // 인증 실패 (401)
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"인증이 필요합니다\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 권한 부족 (403)
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"접근 권한이 없습니다\"}");
                })
        )
        .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class);
        
    // OAuth2 설정은 ClientRegistrationRepository 빈이 있을 때만 적용
    if (context.getBeanProvider(ClientRegistrationRepository.class).getIfAvailable() != null) {
        http.oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo -> 
                userInfo.userService(customOAuth2UserService)
            )
            .successHandler(oAuth2LoginSuccessHandler)
            .failureHandler(oAuth2LoginFailureHandler)
        );
    } 
    return http.build();
    }
}