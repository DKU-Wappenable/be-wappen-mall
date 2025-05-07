package com.wappenable.be.users.handler;

import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null) {
            log.error("OAuth2 로그인 실패: 사용자 이메일이 null입니다.");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "이메일 동의가 필요합니다.");
            return;
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);
        User user;

        if (optionalUser.isEmpty()) {
            // nickname이 없으면 기본값으로 설정
            String nickname = oAuth2User.getAttribute("name");
            if (nickname == null) {
                nickname = "user_" + System.currentTimeMillis();
            }

            user = User.builder()
                    .email(email)
                    .nickname(nickname)
                    .passwordHash(null) // 소셜 로그인 사용자는 비밀번호 없음
                    .role(Role.USER)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);
            log.info("신규 OAuth2 사용자 등록 완료: {}", email);
        } else {
            user = optionalUser.get();
        }

        // JWT 생성
        String accessToken = jwtUtil.generateAccessToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        // 리다이렉트 URL에 토큰 포함
        String redirectUrl = "http://localhost:5173/oauth/success?accessToken=" + accessToken + "&refreshToken=" + refreshToken;
        response.sendRedirect(redirectUrl);
    }
}
