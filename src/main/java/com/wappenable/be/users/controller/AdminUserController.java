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

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;
    private final SimpMessagingTemplate messagingTemplate;


    // 로그인 후 관리자 대시보드
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

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserListDto>> getUserList(
            @PageableDefault(size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<UserListDto> users = adminUserService.getUsersPage(pageable);
        return ResponseEntity.ok(users);
    }

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
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
