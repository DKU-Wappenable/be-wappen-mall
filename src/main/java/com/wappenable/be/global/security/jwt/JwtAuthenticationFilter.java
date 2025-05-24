package com.wappenable.be.global.security.jwt;

import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    // 로그인, 회원가입 시에는 JWT 예외
    private static final List<String> NO_AUTH_URLS = List.of(
        "/api/users/signup",
        "/api/users/login"
    );

    private boolean isNoAuthRequired(String uri) {
        return NO_AUTH_URLS.stream().anyMatch(uri::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (isNoAuthRequired(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = parseToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);
            log.debug("Extracted email from JWT: {}", email);

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                CustomUserDetails userDetails = new CustomUserDetails(user);  // ✅ 핵심: 직접 만든 객체 사용

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("✅ SecurityContext에 CustomUserDetails 등록 완료: {}", user.getEmail());
            } else {
                log.warn("❌ 해당 이메일로 사용자를 찾을 수 없습니다: {}", email);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String parseToken(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }
}
