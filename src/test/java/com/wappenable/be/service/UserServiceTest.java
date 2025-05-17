package com.wappenable.be.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wappenable.be.global.exception.CustomException;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.service.UserService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private SignupRequestDto signupRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequestDto();
        signupRequest.setEmail("test@example.com");
        signupRequest.setNickname("테스트유저");
        signupRequest.setPassword("password123");
        signupRequest.setConfirmPassword("password123");
        signupRequest.setRole(Role.USER);
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
    void 회원가입_실패_비밀번호_불일치() {
        // given
        signupRequest.setConfirmPassword("differentPassword");

        // when & then
        assertThatThrownBy(() -> userService.signup(signupRequest))
            .isInstanceOf(CustomException.class)
            .hasMessage("비밀번호와 비밀번호 확인이 일치하지 않습니다");

        verify(userRepository, never()).save(any(User.class));
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
}