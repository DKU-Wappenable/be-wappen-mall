package com.wappenable.be.global.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.wappenable.be.global.security.jwt.JwtAuthenticationFilter;
import com.wappenable.be.global.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.wappenable.be.global.security.oauth2.handler.OAuth2LoginSuccessHandler;
import com.wappenable.be.global.security.oauth2.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // TODO : @PreAuthorized 등 사용가능하다. , (prePostEnabled = true) 는 뭐임?
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
    SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigSource))
            .csrf(csrf -> csrf.disable()) // [x]: csrf.disable() -> REST API에서는 CSRF 비활성화가 일반적, 대신 JWT, OAuth2 등 토큰 기반 인증 방식 사용
            // 현재 인증 방식 : JWT, 세션 저장이 필요 없는데 Spring Security는 기본적으로 세션에 인증 정보를 자동 저장하려고 시도함
            .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)  
            )
            // [x] : Role 권한마다 접속 가능한 경로 지정
            .authorizeHttpRequests(auth -> auth
                // 공개 API (비로그인 접근 허용)
                .requestMatchers(
                    "/", 
                    "/api/users/signup", 
                    "/api/users/login",
                    "/api/users/find-id", // 아이디 찾기
                    "/api/users/find-pw", // 비밀번호 찾기(초기화)
                    "/oauth2/**", 
                    "/error",
                    "/api/products", // 상품 전체 조회
                    "/api/products/*", // 상품 상세 조회
                    "/favicon.ico"
                ).permitAll()
                
                // Swagger 관련 경로 허용 (개발 환경에서 API 문서 접근을 위해)
                .requestMatchers(
                    "/swagger-ui/**",           // Swagger UI 리소스
                    "/swagger-ui.html",         // Swagger UI 메인 페이지
                    "/v3/api-docs/**",          // OpenAPI 3.0 문서
                    "/v3/api-docs",             // OpenAPI 3.0 문서 루트
                    "/swagger-resources/**",    // Swagger 리소스
                    "/webjars/**"              // Swagger UI에서 사용하는 웹 리소스
                ).permitAll()
                
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // 내부적으로 "ROLE_ADMIN" 검사
                .requestMatchers("/api/users/**").hasAnyRole("USER", "SHOP_OWNER", "ADMIN")
                
                // 커스터마이징 기능
                .requestMatchers(
                    "/api/custom-images", // 상품 이미지 리스트 반환
                    "/api/custom-images/save" // 커스터마이징 결과 저장
                ).hasAnyRole("USER", "SHOP_OWNER", "ADMIN")

                // 상품 등록,수정,삭제,대량등록
                .requestMatchers(
                    "/api/products", // 상품 등록
                    "/api/products/*", // 상품 수정,삭제
                    "/api/products/bulk" // 상품 대량 등록
                ).hasAnyRole("SHOP_OWNER", "ADMIN")
                .requestMatchers(
                    "/api/orders", // 주문
                    "/api/orders/*/cancel").hasRole("USER") // 주문 취소 
                .requestMatchers("/api/orders/user").hasRole("USER") // 소비자용 주문 조회
                .requestMatchers("/api/orders/list").hasAnyRole("SHOP_OWNER", "ADMIN") // 관리자용 전체 주문 목록 조회
                .requestMatchers("/api/orders/*").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 주문 상세 정보 조회
                .requestMatchers("/api/orders/checkout").hasRole("USER") // 장바구니 주문
                .requestMatchers("/api/cart/*").hasRole("USER") // 장바구니 추가 및 조회
                .requestMatchers("/api/cart/id").hasRole("USER") // 장바구니 물품 삭제
                .requestMatchers("/api/cart/cartItemId").hasRole("USER") // 장바구니 물품 수령 수정
                .requestMatchers("/api/cart/checkout").hasRole("USER") // 장바구니 결제


                // 주문 상태 변경,삭제 관련
                .requestMatchers(
                    "/api/orders/*/confirm-deposit", // 무통장 입금시 결제 상태 대기중
                    "/api/orders/*" // 주문 데이터 삭제
                ).hasAnyRole("SHOP_OWNER", "ADMIN")
                .anyRequest().authenticated()
            )
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