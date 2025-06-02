package com.wappenable.be.users.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.users.dto.request.DeleteUserRequestDto;
import com.wappenable.be.users.dto.request.FindEmailRequestDto;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
import com.wappenable.be.users.dto.request.LoginRequestDto;
import com.wappenable.be.users.dto.request.ResetPasswordRequestDto;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequestDto request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDto request) {
        TokenResponse response = userService.login(request);    
        return ResponseEntity.ok(response);
    }

    // 내 정보 조회
    // TODO : 내 정보 조회 시 어떤 값을 프론트에서 보여주는지 일치시키기 / 서비스 로직에 작성 안하고 바로 반환?
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of(
            "email", userDetails.getUser().getEmail(),
            "recoveryEmail", userDetails.getUser().getRecoveryEmail(),
            "nickname", userDetails.getUser().getNickname(),
            "role", userDetails.getUser().getRole().name()
        ));
    }

    // // TODO: 로그아웃
    // @PostMapping("/logout")
    // public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
    //     userService.logout(userDetails.getUser().getEmail());
    //     return ResponseEntity.ok("로그아웃 되었습니다.");
    // }
    

    // 회원탈퇴
    @PostMapping("/withdraw")
    public ResponseEntity<?> deleteUser(@RequestBody @Valid DeleteUserRequestDto request) {
        userService.deleteCurrentUser(request);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }


    // 아이디 찾기
    @PostMapping("/find-id")
    public ResponseEntity<?> findEmail(@Valid @RequestBody FindEmailRequestDto request) {
        String email = userService.findEmailByRecoveryEmail(request.getRecoveryEmail());
        return ResponseEntity.ok(email);
    }

    // 비밀번호 찾기 -> 초기화
    @PostMapping("/find-pw")
    public ResponseEntity<?> findPassword(@Valid @RequestBody FindPasswordRequestDto request) {
        String tempPassword = userService.resetPasswordWithTempPassword(request);
        return ResponseEntity.ok(tempPassword);
    }

    // 비밀번호 재설정
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        userService.resetPassword(request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

} 