package com.wappenable.be.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.users.dto.request.SignupRequest;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignupRequest signupRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setNickname("테스트유저");
        signupRequest.setPassword("password123");
        signupRequest.setRole("USER");
    }

    @Test
    void 회원가입_성공() {
        // given
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(signupRequest.getPassword())).thenReturn("encodedPassword");

        // when
        userService.signup(signupRequest);

        // then
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void 회원가입_실패_중복이메일() {
        // given
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(CustomException.class)
                .hasMessage("중복된 이메일입니다");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 역할_변경_성공() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@wappen.com")
                .nickname("테스트")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when
        userService.updateUserRole(userId, "ADMIN");

        // then
        verify(userRepository).findById(userId);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    void 역할_변경_실패_잘못된_역할() {
        // given
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .email("test@wappen.com")
                .nickname("테스트")
                .role(Role.USER)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // when & then
        assertThatThrownBy(() -> userService.updateUserRole(userId, "INVALID_ROLE"))
                .isInstanceOf(CustomException.class)
                .hasMessage("잘못된 역할 값입니다");
    }

    @Test
    void 역할_변경_실패_없는_사용자() {
        // given
        Long userId = 99L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.updateUserRole(userId, "ADMIN"))
                .isInstanceOf(CustomException.class)
                .hasMessage("사용자 없음");
    }

    @Test
    void 전체_사용자_조회_성공() {
        // given
        List<User> users = List.of(
            User.builder()
                .id(1L)
                .email("user1@example.com")
                .nickname("유저1")
                .passwordHash("encoded1")
                .role(Role.USER)
                .build(),
            User.builder()
                .id(2L)
                .email("admin@example.com")
                .nickname("관리자")
                .passwordHash("encoded2")
                .role(Role.ADMIN)
                .build()
        );

        when(userRepository.findAll()).thenReturn(users);

        // when
        var result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getEmail()).isEqualTo("user1@example.com");
        assertThat(result.get(0).getRole()).isEqualTo("USER");
        assertThat(result.get(1).getNickname()).isEqualTo("관리자");
        assertThat(result.get(1).getRole()).isEqualTo("ADMIN");
    }
}
