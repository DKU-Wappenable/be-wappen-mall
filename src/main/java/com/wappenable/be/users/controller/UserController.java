package com.wappenable.be.users.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
// 종진 추가
import org.springframework.web.bind.annotation.PutMapping;
import com.wappenable.be.users.dto.request.UpdateUserRequestDto;
import com.wappenable.be.terms.repository.UserTermsAgreementRepository;
import com.wappenable.be.users.dto.response.UserListDto; // 🔧 추가
import com.wappenable.be.users.domain.User;
// Swagger 애노테이션 추가
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.global.security.jwt.TokenResponse;
import com.wappenable.be.terms.dto.request.AgreeTermsRequestDto;
import com.wappenable.be.terms.service.AgreeTermsService;
import com.wappenable.be.users.dto.request.DeleteUserRequestDto;
import com.wappenable.be.users.dto.request.FindEmailRequestDto;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
import com.wappenable.be.users.dto.request.LoginRequestDto;
import com.wappenable.be.users.dto.request.ResetPasswordRequestDto;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.dto.response.LogoutResponse;
import com.wappenable.be.users.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User API", description = "사용자 관련 API - 회원가입, 로그인, 비밀번호 관리 등의 기능을 제공합니다")
public class UserController {

    private final UserService userService;
    private final AgreeTermsService agreeTermsService;
    private final UserTermsAgreementRepository userTermsAgreementRepository;

    // 회원가입
    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터 또는 이미 존재하는 이메일"),
        @ApiResponse(responseCode = "409", description = "중복된 이메일")
    })
    public ResponseEntity<?> signup(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "회원가입 정보", 
            required = true,
            content = @Content(schema = @Schema(implementation = SignupRequestDto.class))
        )
        @Valid @RequestBody SignupRequestDto request
    ) {
        userService.signup(request);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    @Operation(summary = "로그인", description = "사용자 인증을 수행하고 JWT 토큰을 반환합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공",
                content = @Content(schema = @Schema(implementation = TokenResponse.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "잘못된 이메일 또는 비밀번호")
    })
    public ResponseEntity<?> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "로그인 정보", 
            required = true,
            content = @Content(schema = @Schema(implementation = LoginRequestDto.class))
        )
        @Valid @RequestBody LoginRequestDto request
    ) {
        TokenResponse response = userService.login(request);    
        return ResponseEntity.ok(response);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
    User user = userDetails.getUser();
    boolean agreed = userTermsAgreementRepository.existsByUserAndFirstIsTrueAndCheckedIsTrue(user);
    return ResponseEntity.ok(UserListDto.from(user, agreed));
}


    // 종진 비밀번호 재설정 관련 put 매핑
    // @PutMapping("/me")
    // public ResponseEntity<?> updateCurrentUser(
    // @AuthenticationPrincipal CustomUserDetails userDetails,
    // @RequestBody UpdateUserRequestDto request
    // ) {
    //     userService.updateUser(userDetails.getUser(), request);
    //     return ResponseEntity.ok(UserListDto.from(updatedUser, true)); // 실제 업데이트된 사용자 정보 반환
    // }

    // TODO: 로그아웃 1. 일반 사용자 2. 소셜 계정 사용자(카카오,구글,네이버)
    // public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
    //     LogoutResponse response = userService.logout(userDetails.getUser());
    //     return ResponseEntity.ok(response);
    // }


    // 회원탈퇴
    @PostMapping("/withdraw")
    public ResponseEntity<?> deleteUser(@RequestBody @Valid DeleteUserRequestDto request) {
        userService.deleteCurrentUser(request);
        return ResponseEntity.ok("회원 탈퇴 완료");
    }

    // 아이디 찾기
    @PostMapping("/find-id")
    @Operation(summary = "아이디 찾기", description = "복구 이메일을 통해 가입된 이메일(아이디)을 찾습니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "아이디 찾기 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "해당 복구 이메일로 가입된 계정이 없음")
    })
    public ResponseEntity<?> findEmail(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "복구 이메일 정보", 
            required = true,
            content = @Content(schema = @Schema(implementation = FindEmailRequestDto.class))
        )
        @Valid @RequestBody FindEmailRequestDto request
    ) {
        String email = userService.findEmailByRecoveryEmail(request.getRecoveryEmail());
        return ResponseEntity.ok(email);
    }

    // 비밀번호 찾기 -> 초기화
    @PostMapping("/find-pw")
    @Operation(summary = "비밀번호 찾기/초기화", description = "임시 비밀번호를 생성하여 사용자에게 제공합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "임시 비밀번호 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "해당 이메일로 가입된 계정이 없음")
    })
    public ResponseEntity<?> findPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "비밀번호 찾기 정보", 
            required = true,
            content = @Content(schema = @Schema(implementation = FindPasswordRequestDto.class))
        )
        @Valid @RequestBody FindPasswordRequestDto request
    ) {
        String tempPassword = userService.resetPasswordWithTempPassword(request);
        return ResponseEntity.ok(tempPassword);
    }
    // 약관동의 
    @PutMapping("/agree-terms")
    public ResponseEntity<?> agreeTerms(
        @AuthenticationPrincipal CustomUserDetails userDetails,
        @RequestBody AgreeTermsRequestDto request
    ) {
        User user = userDetails.getUser();
        agreeTermsService.saveAgreement(user, request);
        return ResponseEntity.ok("약관 동의 완료");
    }

    // 비밀번호 재설정
    @PutMapping("/reset-password")
    @Operation(summary = "비밀번호 재설정", description = "새로운 비밀번호로 변경합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<?> resetPassword(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "비밀번호 재설정 정보", 
            required = true,
            content = @Content(schema = @Schema(implementation = ResetPasswordRequestDto.class))
        )
        @Valid @RequestBody ResetPasswordRequestDto request
    ) {
        userService.resetPassword(request);
        return ResponseEntity.ok("비밀번호가 성공적으로 변경되었습니다.");
    }

} 