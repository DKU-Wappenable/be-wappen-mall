package com.wappenable.be.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;

import com.wappenable.be.global.exception.users.EmailAlreadyExistsException;
import com.wappenable.be.global.exception.users.PasswordMismatchException;
import com.wappenable.be.global.exception.users.RecoveryEmailNotFoundException;
import com.wappenable.be.global.exception.users.ResetPasswordNotAllowedException;
import com.wappenable.be.global.exception.users.UserNotFoundException;
import com.wappenable.be.global.exception.users.UserRecoveryMismatchException;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
import com.wappenable.be.users.dto.request.ResetPasswordRequestDto;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import com.wappenable.be.users.service.UserService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    private SignupRequestDto signupRequest;

    @BeforeEach
    void setup() {
        signupRequest = new SignupRequestDto();
        signupRequest.setEmail("testuser");
        signupRequest.setRecoveryEmail("recovery@example.com");
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
            .isInstanceOf(PasswordMismatchException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 회원가입_실패_중복아이디() {
        // given
        when(userRepository.existsByEmail(signupRequest.getEmail())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.signup(signupRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void 아이디찾기_성공(){
        String expectedEmail = "foundId";
        when(userRepository.findByRecoveryEmail("recovery@example.com"))
            .thenReturn(Optional.of(User.builder().email(expectedEmail).build()));

        String result = userService.findEmailByRecoveryEmail("recovery@example.com");

        assertThat(result).isEqualTo(expectedEmail);
    }

    @Test
    void 아이디찾기_실패() {
        // given
        when(userRepository.findByRecoveryEmail("invalid@example.com"))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findEmailByRecoveryEmail("invalid@example.com"))
            .isInstanceOf(RecoveryEmailNotFoundException.class);
    }


    @Test
    void 비밀번호초기화_성공() {
        // given
        FindPasswordRequestDto request = new FindPasswordRequestDto("user", "recovery@example.com");
        User user = User.builder().email("user").recoveryEmail("recovery@example.com").build();

        when(userRepository.findByEmailAndRecoveryEmail(request.getEmail(), request.getRecoveryEmail()))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedTempPassword");

        // when
        String tempPassword = userService.resetPasswordWithTempPassword(request);

        // then
        assertThat(tempPassword).hasSize(10); // UUID substring 길이 확인
        verify(userRepository).findByEmailAndRecoveryEmail(request.getEmail(), request.getRecoveryEmail());
        verify(passwordEncoder).encode(tempPassword);
    }

    @Test
    void 비밀번호초기화_실패() {
        // given
        FindPasswordRequestDto request = new FindPasswordRequestDto("wrong", "wrongRecovery@example.com");

        when(userRepository.findByEmailAndRecoveryEmail(request.getEmail(), request.getRecoveryEmail()))
            .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.resetPasswordWithTempPassword(request))
            .isInstanceOf(UserRecoveryMismatchException.class);
    }

    @Test
    void 비밀번호재설정_성공() {
        // given
        // SecurityContext에 인증된 사용자 설정
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("testuser", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResetPasswordRequestDto request = new ResetPasswordRequestDto("testuser", "Newpass123!");
        User user = User.builder()
                .email("testuser")
                .passwordHash("oldPasswordHash")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.encode(request.getNewPassword())).thenReturn("encodedNewPassword");

        // when
        userService.resetPassword(request);

        // then
        assertThat(user.getPasswordHash()).isEqualTo("encodedNewPassword");
        verify(userRepository).findByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getNewPassword());
    }

    @Test
    void 비밀번호재설정_실패_본인계정아님() {

        // 인증 사용자와 요청 이메일이 일치해야 함
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("test@example.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResetPasswordRequestDto request = new ResetPasswordRequestDto("other@example.com", "Newpass123!");

        assertThatThrownBy(() -> userService.resetPassword(request))
            .isInstanceOf(ResetPasswordNotAllowedException.class);
    }

    @Test
    void 비밀번호재설정_실패_사용자없음() {

        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken("unknown", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResetPasswordRequestDto request = new ResetPasswordRequestDto("unknown", "Newpass123!");
        when(userRepository.findByEmail("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.resetPassword(request))
            .isInstanceOf(UserNotFoundException.class);
    }
}