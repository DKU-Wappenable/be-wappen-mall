package com.wappenable.be.users.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.users.dto.request.LoginRequest;
import com.wappenable.be.users.dto.request.SignupRequest;
import com.wappenable.be.users.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// 회원가입 컨트롤러
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse response = userService.login(request);    
        return ResponseEntity.ok(response);
    }
    
    // TODO: 로그아웃 추가
    // TODO: 아이디/비밀번호 찾기
    // TODO: 회원 탈퇴
} 