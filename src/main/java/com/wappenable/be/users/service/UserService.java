package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.global.exception.users.CurrentUserNotFoundException;
import com.wappenable.be.global.exception.users.EmailAlreadyExistsException;
import com.wappenable.be.global.exception.users.InvalidPasswordException;
import com.wappenable.be.global.exception.users.PasswordMismatchException;
import com.wappenable.be.global.exception.users.RecoveryEmailNotFoundException;
import com.wappenable.be.global.exception.users.ResetPasswordNotAllowedException;
import com.wappenable.be.global.exception.users.UnauthorizedAccessException;
import com.wappenable.be.global.exception.users.UserNotFoundException;
import com.wappenable.be.global.exception.users.UserRecoveryMismatchException;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.request.DeleteUserRequestDto;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
import com.wappenable.be.users.dto.request.LoginRequestDto;
import com.wappenable.be.users.dto.request.ResetPasswordRequestDto;
import com.wappenable.be.users.dto.request.SignupRequestDto;
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

    // 로그아웃
    // @Transactional
    // public void logout(String email) {
    //     com.wappenable.be.users.domain.User user = userRepository.findByEmail(email)
    //         .orElseThrow(UserNotFoundException::new);
    
    //     // Refresh Token 제거 (일반 사용자든 소셜 사용자든 공통)
    //     user.setRefreshToken(null);
    
    //     // 소셜 사용자에 대해 추가 처리 필요 시 분기
    //     if (user.getProvider() != null) {
    //         log.info("소셜 사용자 로그아웃 처리: " + user.getProvider());
    //         // 필요 시 Kakao, Google API 호출해서 세션 해제 (선택 사항)
    //     }
    
    //     userRepository.save(user);
    // }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
            "user ID", userDetails.getUser().getEmail(),
            "email", userDetails.getUser().getRecoveryEmail(),
            "nickname", userDetails.getUser().getNickname(),
            "role", userDetails.getUser().getRole().name()
        ));
    }

    // 회원 탈퇴
    @Transactional
    public void deleteCurrentUser(DeleteUserRequestDto request) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // 1. 요청한 email과 현재 로그인한 사용자 email이 같은지 확인
        if (!currentEmail.equals(request.getEmail())) {
            throw new UnauthorizedAccessException();
        }

        User user = userRepository.findByEmail(currentEmail)
            .orElseThrow(CurrentUserNotFoundException::new);

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        // 3. 삭제
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

    // 비밀번호 재설정
    // TODO 이후에 본인 비밀번호도 추가하는 걸로, 필수 기능 먼저 구현하자.
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!currentUserEmail.equals(request.getEmail())) {
            throw new ResetPasswordNotAllowedException();
        }

        User user = userRepository.findByEmail(currentUserEmail)
            .orElseThrow(UserNotFoundException::new);

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(encodedPassword);
    }


    
}
