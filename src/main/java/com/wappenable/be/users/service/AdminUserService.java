package com.wappenable.be.users.service;

import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.users.dto.response.UserListDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    @Transactional
    public void updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new CustomException("사용자 없음", HttpStatus.BAD_REQUEST));

        try {
            Role newRole = Role.valueOf(roleName.toUpperCase());
            user.setRole(newRole);
        } catch (IllegalArgumentException e) {
            throw new CustomException("잘못된 역할 값입니다", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional(readOnly = true)
    public List<UserListDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserListDto(
                        user.getId(),
                        user.getEmail(),
                        user.getNickname(),
                        user.getRole().name()
                ))
                .collect(Collectors.toList());
    }
}
