package com.wappenable.be.users.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wappenable.be.users.dto.request.RoleUpdateRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.service.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {
    private final UserService userService;

    // 권한을 변경하는 API
    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id,
                                               @RequestBody RoleUpdateRequestDto dto) {
        userService.updateUserRole(id, dto.getRole());
        return ResponseEntity.ok().build();
    }

    // Role 드롭다운
    @GetMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> getAvailableRoles() {
        List<String> roles = Arrays.stream(Role.values())
            .map(Enum::name)
            .toList();
        return ResponseEntity.ok(roles);
    }
    

}
