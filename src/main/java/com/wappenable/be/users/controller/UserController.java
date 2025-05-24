package com.wappenable.be.users.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wappenable.be.global.security.jwt.TokenResponse;
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
    
    // TODO: 로그아웃

    // 회원탈퇴
    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        userService.deleteCurrentUser();
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