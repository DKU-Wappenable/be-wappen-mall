package com.wappenable.be.global.security.oauth2.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.repository.UserRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();

        String provider = oauthToken.getAuthorizedClientRegistrationId(); // ex) naver, google, kakao
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isBlank()) {
            log.error("OAuth2 로그인 실패: 이메일이 누락되었습니다.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "이메일이 필요합니다.");
            return;
        }

        log.info("OAuth2 로그인 성공: provider={}, email={}", provider, email);

        // DB에서 User 조회
        User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("OAuth2 로그인 유저가 DB에 없습니다."));

        // JWT 발급
        String accessToken = jwtUtil.generateAccessToken(email, user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(email, user.getRole().name());

        // TODO 프론트와 리다이렉트 주소 맞는지 확인 필요
        String redirectUrl = "http://localhost:5173/oauth/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        response.sendRedirect(redirectUrl);
    }
}

