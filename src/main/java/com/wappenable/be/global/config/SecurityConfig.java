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
import org.springframework.web.cors.CorsConfigurationSource;

import com.wappenable.be.global.security.jwt.JwtAuthenticationFilter;
import com.wappenable.be.global.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.wappenable.be.global.security.oauth2.handler.OAuth2LoginSuccessHandler;
import com.wappenable.be.global.security.oauth2.service.CustomOAuth2UserService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
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
    SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigSource) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",
                    "/api/users/signup",
                    "/api/users/login",
                    "/api/users/find-id", // 아이디 찾기
                    "/api/users/find-pw", // 비밀번호 찾기(초기화)
                    "/api/users/reset-password", // 비밀번호 재설정
                    "/oauth2/**", 
                    "/error",
                    "/api/products",
                    "/api/products/*",
                    "/favicon.ico",
                    "/uploads/**"
                ).permitAll()

                .requestMatchers("/api/users/**").hasAnyRole("USER", "SHOP_OWNER", "ADMIN")

                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/v3/api-docs",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/users/me").hasAnyRole("USER", "SHOP_OWNER", "ADMIN")

                .requestMatchers(
                    "/api/custom-images",
                    "/api/custom-images/save",
                    "/api/products/publish-custom/*"
                ).hasAnyRole("USER", "SHOP_OWNER", "ADMIN")

                .requestMatchers(
                    "/api/products",
                    "/api/products/*",
                    "/api/products/bulk"
                ).hasAnyRole("SHOP_OWNER", "ADMIN")

                .requestMatchers(
                    "/api/orders", // 주문
                    "/api/orders/*/cancel").hasRole("USER") // 주문 취소 
                .requestMatchers("/api/orders/user").hasRole("USER") // 소비자용 주문 조회
                .requestMatchers("/api/orders/list")hasAnyRole("SHOP_OWNER", "ADMIN") // 관리자용 전체 주문 목록 조회
                .requestMatchers("/api/orders/*").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 주문 상세 정보 조회
                .requestMatchers("/api/orders/checkout").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 장바구니 주문
                .requestMatchers("/api/cart/*").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 장바구니 추가 및 조회
                .requestMatchers("/api/cart/id").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 장바구니 물품 삭제
                .requestMatchers("/api/cart/cartItemId").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 장바구니 물품 수령 수정
                .requestMatchers("/api/cart/checkout").hasAnyRole("USER", "SHOP_OWNER", "ADMIN") // 장바구니 결제


                // 주문 상태 변경,삭제 관련
                .requestMatchers(
                    "/api/orders/*/confirm-deposit",
                    "/api/orders/*"
                ).hasAnyRole("SHOP_OWNER", "ADMIN")

                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"인증이 필요합니다\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setCharacterEncoding("UTF-8");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\": \"접근 권한이 없습니다\"}");
                })
            )
            .addFilterBefore(jwtAuthenticationFilter, AnonymousAuthenticationFilter.class);

        if (context.getBeanProvider(ClientRegistrationRepository.class).getIfAvailable() != null) {
            http.oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo.userService(customOAuth2UserService))
                .successHandler(oAuth2LoginSuccessHandler)
                .failureHandler(oAuth2LoginFailureHandler)
            );
        }

        return http.build();
    }
}
