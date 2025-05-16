package com.wappenable.be.users.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wappenable.be.users.dto.request.RoleUpdateRequestDto;
import com.wappenable.be.users.dto.response.RoleChangedMessage;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    // 50명 초기 목록 렌더링을 위해 필요
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListDto>> getUserList() {
        List<UserListDto> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // 권한 변경 API + WebSocket 브로드캐스트
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id,
                                               @RequestBody RoleUpdateRequestDto req) {
        userService.updateUserRole(id, req.getRole());

        // WebSocket 실시간 알림 전송
        messagingTemplate.convertAndSend("/topic/users/role",
                new RoleChangedMessage(id, req.getRole(), "권한이 변경되었습니다"));
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
    

}
