package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.users.dto.request.LoginRequestDto;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new CustomException("비밀번호와 비밀번호 확인이 일치하지 않습니다", HttpStatus.BAD_REQUEST);
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new CustomException("이미 존재하는 아이디입니다.", HttpStatus.CONFLICT);
        }

        // TODO : 일단 회원가입 시 Role 선택 필드는 없는걸로, 기본은 USER, 추후 디벨롭
        Role role = request.getRole() != null ? request.getRole() : Role.USER;

        User user = User.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                // .createdAt(LocalDateTime.now()) 생략 가능 : @CreatedAt
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new CustomException("아이디가 존재하지 않습니다.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void deleteCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        userRepository.delete(user);
    }
}
