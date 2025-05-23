package com.wappenable.be.service;

import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.service.AdminUserService;
import com.wappenable.be.users.service.UserService;
import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.global.exception.admin.AdminUserNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock private UserRepository userRepository;
    @InjectMocks private AdminUserService adminUserService;
    @Mock private PasswordEncoder passwordEncoder;
    private SignupRequestDto signupRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequestDto();
        signupRequest.setEmail("testuser");
        signupRequest.setRecoveryEmail("recovery@example.com");
        signupRequest.setNickname("테스트유저");
        signupRequest.setPassword("password123");
        signupRequest.setRole(Role.USER);
    }

    @Test
    void 역할_변경_성공() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("testuser")
                .recoveryEmail("recovery@example.com")
                .nickname("테스트")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        adminUserService.updateUserRole(userId, Role.ADMIN);

        // then
        verify(userRepository).findById(userId);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void 역할_변경_실패_없는_사용자() {
        // given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> adminUserService.updateUserRole(userId, Role.ADMIN))
                .isInstanceOf(AdminUserNotFoundException.class);
    }

    @Test
    void 전체_사용자_조회_성공() {
        // given
        List<User> users = List.of(
            User.builder()
                .id(1L)
                .email("user1")
                .recoveryEmail("recovery1@example.com")
                .nickname("유저1")
                .passwordHash("encoded1")
                .role(Role.USER)
                .build(),
            User.builder()
                .id(2L)
                .email("admin")
                .recoveryEmail("recovery2@example.com")
                .nickname("관리자")
                .passwordHash("encoded2")
                .role(Role.ADMIN)
                .build()
        );

        when(userRepository.findAll()).thenReturn(users);

        // when
        var result = adminUserService.getAllUsers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("user1");
        assertThat(result.get(0).getRecoveryEmail()).isEqualTo("recovery1@example.com");
        assertThat(result.get(0).getRole()).isEqualTo(Role.USER);

        assertThat(result.get(1).getNickname()).isEqualTo("관리자");
        assertThat(result.get(1).getRecoveryEmail()).isEqualTo("recovery2@example.com");
        assertThat(result.get(1).getRole()).isEqualTo(Role.ADMIN);
    }
}
