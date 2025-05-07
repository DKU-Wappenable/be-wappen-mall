package com.wappenable.be.users.filter;

import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (NO_AUTH_URLS.contains(uri)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = parseToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            String email = jwtUtil.extractEmail(token);

            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                User user = userOptional.get();
                UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                        .username(user.getEmail())
                        .password("")
                        .authorities(user.getRole().toGrantedAuthority())
                        .build();

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
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
