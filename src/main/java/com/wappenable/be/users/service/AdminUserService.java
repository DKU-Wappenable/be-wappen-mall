package com.wappenable.be.users.service;


import com.wappenable.be.global.exception.admin.AdminUserNotFoundException;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
            .orElseThrow(AdminUserNotFoundException::new);

        user.setRole(newRole);
    }

    // [ ] : 이건 전체 사용자 조회이지 50명 초기 목록 렌더링이 아님
    @Transactional(readOnly = true)
    public List<UserListDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserListDto(
                        user.getId(),
                        user.getEmail(),
                        user.getRecoveryEmail(),
                        user.getNickname(),
                        user.getRole()
                ))
                .collect(Collectors.toList());
    }
}
