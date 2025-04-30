package com.wappenable.be.users.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.wappenable.be.users.dto.SignupRequest;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.exception.CustomException;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.service.UserService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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
}
