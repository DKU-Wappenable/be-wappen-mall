package com.wappenable.be.users.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;



import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.dto.request.RoleUpdateRequestDto;
import com.wappenable.be.users.dto.response.RoleChangedMessageDto;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.service.AdminUserService;
import com.wappenable.be.users.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Admin API", description = "관리자 권한이 필요한 사용자 관리 API입니다.")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;
    private final SimpMessagingTemplate messagingTemplate;


    // 로그인 후 관리자 대시보드
    @Operation(
        summary = "관리자 권한 확인",
        description = "로그인한 사용자가 관리자 권한을 가지고 있는지 확인합니다.",
        security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "권한 확인 성공"),
        @ApiResponse(responseCode = "401", description = "JWT 토큰 없음 또는 만료"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/check-auth")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> checkAdminAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(Map.of("role", userDetails.getUser().getRole().name()));
    }

    // 50명 초기 목록 렌더링을 위해 필요
    // @GetMapping("/users")
    // @PreAuthorize("hasRole('ADMIN')")
    // public ResponseEntity<List<UserListDto>> getUserList() {
    //     List<UserListDto> users = adminUserService.getAllUsers();
    //     return ResponseEntity.ok(users);
    // }
    
    @Operation(
        summary = "사용자 목록 조회 (페이지네이션)",
        description = "전체 사용자 목록을 50개씩 페이지네이션 형태로 조회합니다.",
        security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserListDto>> getUserList(
            @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserListDto> users = adminUserService.getUsersPage(pageable);
        return ResponseEntity.ok(users);
    }

    @Operation(
        summary = "사용자 권한 변경",
        description = "특정 사용자의 권한을 변경하고 WebSocket으로 실시간 알림을 전송합니다.",
        security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "권한 변경 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 권한 값"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 사용자 없음")
    })
    // 권한 변경 API + WebSocket 브로드캐스트
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id,
                                               @RequestBody RoleUpdateRequestDto req) {
                                                adminUserService.updateUserRole(id, req.getRole());

        // WebSocket 실시간 알림 전송
        messagingTemplate.convertAndSend("/topic/users/role",
                new RoleChangedMessageDto(id, req.getRole(), "권한이 변경되었습니다"));
        return ResponseEntity.ok().build();

    }

    @Operation(
        summary = "사용 가능한 권한 목록 조회",
        description = "Enum으로 정의된 Role 목록을 반환합니다.",
        security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "권한 목록 조회 성공"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음")
    })
    // Role 드롭다운 목록
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAvailableRoles() {
        List<String> roles = Arrays.stream(Role.values())
            .map(Enum::name)
            .toList();
        return ResponseEntity.ok(roles);
    }

    // 관리자 - 사용자 삭제
    @Operation(
        summary = "사용자 삭제",
        description = "특정 사용자를 삭제합니다.",
        security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 삭제 성공"),
        @ApiResponse(responseCode = "403", description = "관리자 권한 없음"),
        @ApiResponse(responseCode = "404", description = "해당 사용자 없음")
    })
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
