package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wappenable.be.global.exception.users.CurrentUserNotFoundException;
import com.wappenable.be.global.exception.users.EmailAlreadyExistsException;
import com.wappenable.be.global.exception.users.InvalidPasswordException;
import com.wappenable.be.global.exception.users.PasswordMismatchException;
import com.wappenable.be.global.exception.users.RecoveryEmailNotFoundException;
import com.wappenable.be.global.exception.users.UserNotFoundException;
import com.wappenable.be.global.exception.users.UserRecoveryMismatchException;
import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
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
            throw new PasswordMismatchException();
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        // TODO : 일단 회원가입 시 Role 선택 필드는 없는걸로, 기본은 USER, 추후 디벨롭
        Role role = request.getRole() != null ? request.getRole() : Role.USER;

        User user = User.builder()
                .email(request.getEmail())
                .recoveryEmail(request.getRecoveryEmail())
                .nickname(request.getNickname())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                // .createdAt(LocalDateTime.now()) 생략 가능 : @CreatedAt
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());

        return new TokenResponse(accessToken, refreshToken);
    }

    @Transactional
    public void deleteCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(CurrentUserNotFoundException::new);

        userRepository.delete(user);
    }

    // recoveryEmail로 아이디(email) 찾기
    public String findEmailByRecoveryEmail(String recoveryEmail) {
        return userRepository.findByRecoveryEmail(recoveryEmail)
            .map(User::getEmail)
            .orElseThrow(RecoveryEmailNotFoundException::new);
    }

    // 아이디(email) + 복구용 이메일(recoveryEmail)로 비밀번호 찾기 -> 초기화
    public String resetPasswordWithTempPassword(FindPasswordRequestDto request) {
        User user = userRepository.findByEmailAndRecoveryEmail(request.getEmail(), request.getRecoveryEmail())
            .orElseThrow(UserRecoveryMismatchException::new);
    
        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        return tempPassword;
    }
    
}
