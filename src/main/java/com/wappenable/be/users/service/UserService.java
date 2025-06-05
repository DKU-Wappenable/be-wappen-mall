package com.wappenable.be.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.wappenable.be.global.exception.users.LoginUserNotFoundException;
import com.wappenable.be.global.exception.users.UserRecoveryMismatchException;
import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.global.security.jwt.JwtUtil;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.global.security.oauth2.domain.AuthProvider;
import com.wappenable.be.terms.repository.UserTermsAgreementRepository;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.request.DeleteUserRequestDto;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
import com.wappenable.be.users.dto.request.LoginRequestDto;
import com.wappenable.be.users.dto.request.ResetPasswordRequestDto;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.dto.response.LogoutResponse;
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
                .orElseThrow(LoginUserNotFoundException::new);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidPasswordException();
        }

        // NOTE : 약관 동의 여부 확인
        boolean agreed = userTermsAgreementRepository.existsByUserAndFirstIsTrueAndCheckedIsTrue(user);

        String accessToken = jwtUtil.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail(), user.getRole().name());

        return new TokenResponse(accessToken, refreshToken, agreed);
    }

    // 로그아웃
    // @Transactional
    // public LogoutResponse logout(User user) {
    //     // 1. RefreshToken 제거
    //     user.setRefreshToken(null);

    //     String logoutUrl = null;

    //     // 2. 소셜 로그인 사용자라면 provider 확인
    //     if (!user.getSocialAccounts().isEmpty()) {
    //         AuthProvider provider = user.getSocialAccounts().get(0).getProvider(); // 현재 로그인된 provider로 변경해야 함

    //         switch (provider) {
    //             case GOOGLE -> logoutUrl = "https://accounts.google.com/Logout";
    //             case KAKAO -> logoutUrl = "https://kauth.kakao.com/oauth/logout?client_id=YOUR_KAKAO_CLIENT_ID&logout_redirect_uri=YOUR_REDIRECT_URI";
    //             case NAVER -> logoutUrl = "https://nid.naver.com/nidlogin.logout";
    //         }

    //         log.info("소셜 사용자 로그아웃 처리: {}", provider);
    //     }

    //     userRepository.save(user);
    //     return new LogoutResponse("로그아웃 완료", logoutUrl);
    // }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        boolean agreed = userTermsAgreementRepository
            .findByUser(user)
            .stream()
            .anyMatch(a -> a.isFirst() && a.isChecked());

        return ResponseEntity.ok(Map.of(
            "email", user.getEmail(),
            "nickname", user.getNickname(),
            "recoveryEmail", user.getRecoveryEmail(),
            "role", user.getRole().name(),
            "termsAccepted", agreed
        ));
    }

    /*  TODO : 회원 탈퇴
        1. 일반 사용자
            1-a. 소셜 계정 없는 일반 사용자
            1-b. 소셜 계정도 존재하는 일반 사용자
        2. 소셜 계정만 사용하는 사용자
    */
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
    @Transactional
    public void resetPassword(ResetPasswordRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(LoginUserNotFoundException::new);

        String encodedPassword = passwordEncoder.encode(request.getNewPassword());
        user.setPasswordHash(encodedPassword);
    }


    
}
