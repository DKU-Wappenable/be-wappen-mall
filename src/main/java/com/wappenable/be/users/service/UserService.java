package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import com.wappenable.be.global.exception.users.*;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.terms.repository.UserTermsAgreementRepository;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.request.*;
import com.wappenable.be.users.dto.response.LogoutResponse;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserTermsAgreementRepository userTermsAgreementRepository;
    private final JwtUtil jwtUtil;

    @Transactional
    public void signup(SignupRequestDto request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException();
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        Role role = request.getRole() != null ? request.getRole() : Role.USER;

        User user = User.builder()
                .email(request.getEmail())
                .recoveryEmail(request.getRecoveryEmail())
                .nickname(request.getNickname())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();

        userRepository.save(user);
    }

    public TokenResponse login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginUserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        boolean agreed = userTermsAgreementRepository.existsByUserAndFirstIsTrueAndCheckedIsTrue(user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());

        return new TokenResponse(accessToken, refreshToken, agreed);
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        boolean agreed = userTermsAgreementRepository.existsByUserAndFirstIsTrueAndCheckedIsTrue(user);

        return ResponseEntity.ok(UserListDto.from(user, agreed)); 
    }

    @Transactional
    public User updateUser(User currentUser, UpdateUserRequestDto request) {
        currentUser.setNickname(request.getNickname());
        return userRepository.save(currentUser);
    }

    @Transactional
    public void deleteCurrentUser(DeleteUserRequestDto request) {
        String currentEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        
        if (!currentEmail.equals(request.getEmail())) {
            throw new UnauthorizedAccessException();
        }
        
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(CurrentUserNotFoundException::new);
        
        // 소셜 로그인 사용자 체크 (socialAccounts가 존재하는지 확인)
        boolean isSocialUser = !user.getSocialAccounts().isEmpty();
        
        if (isSocialUser) {
            // 소셜 로그인 사용자는 비밀번호 검증 없이 탈퇴 처리
            log.info("소셜 로그인 사용자 탈퇴 처리: {} (연동된 계정: {}개)", 
                    currentEmail, user.getSocialAccounts().size());
            
            // 어떤 소셜 계정들이 삭제되는지 로깅
            user.getSocialAccounts().forEach(socialAccount ->
                log.info("  - {} 계정 삭제: {}", socialAccount.getProvider(), socialAccount.getProviderUserId())
            );
        } else {
            // 일반 사용자는 비밀번호 검증 필요
            if (user.getPasswordHash() == null || user.getPasswordHash().isEmpty()) {
                throw new InvalidPasswordException();
            }
            
            if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
                throw new InvalidPasswordException();
            }
            
            log.info("일반 사용자 탈퇴 처리: {}", currentEmail);
        }
        
        // 약관 동의 기록이 있다면 로깅
        if (user.getTermsAgreements() != null && !user.getTermsAgreements().isEmpty()) {
            log.info("사용자 {}의 약관 동의 기록 {}개도 함께 삭제", currentEmail, user.getTermsAgreements().size());
        }
        
        // User 삭제 시 cascade로 SocialAccount들도 자동 삭제
        userRepository.delete(user);
        
        log.info("사용자 탈퇴 완료: {}", currentEmail);
    }

    public String findEmailByRecoveryEmail(String recoveryEmail) {
        return userRepository.findByRecoveryEmail(recoveryEmail)
                .map(User::getEmail)
                .orElseThrow(RecoveryEmailNotFoundException::new);
    }

    public String resetPasswordWithTempPassword(FindPasswordRequestDto request) {
        User user = userRepository.findByEmailAndRecoveryEmail(request.getEmail(), request.getRecoveryEmail())
                .orElseThrow(UserRecoveryMismatchException::new);

        String tempPassword = UUID.randomUUID().toString().replace("-", "").substring(0, 10);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        return tempPassword;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginUserNotFoundException::new);

        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new SamePasswordException();
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
    }
}
