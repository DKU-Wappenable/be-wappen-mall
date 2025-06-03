package com.wappenable.be.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wappenable.be.global.exception.users.InvalidPasswordException;
import com.wappenable.be.global.exception.users.UserNotFoundException;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.request.LoginRequestDto;
import com.wappenable.be.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "admin.email=testAdmin",
    "admin.password=testpass123"
})
@Transactional
@SpringBootTest
class UserLoginIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .email("login")
                .recoveryEmail("login@email.com")
                .nickname("로그인유저")
                .passwordHash(passwordEncoder.encode("validPass123!"))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("로그인 성공 - accessToken과 refreshToken 반환")
    void login_success() throws Exception {
        LoginRequestDto request = new LoginRequestDto("login", "validPass123!");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").exists())
            .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 없음")
    void login_fail_emailNotFound() throws Exception {
        LoginRequestDto request = new LoginRequestDto("notfound", "validPass123!");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value(UserNotFoundException.MESSAGE));
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void login_fail_wrongPassword() throws Exception {
        LoginRequestDto request = new LoginRequestDto("login", "wrongPassword!");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.error").value(InvalidPasswordException.MESSAGE));
    }

    @Test
    @DisplayName("로그인 실패 - 아이디 비어 있음")
    void login_fail_blankEmail() throws Exception {
        LoginRequestDto request = new LoginRequestDto("", "somePassword");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 비어 있음")
    void login_fail_blankPassword() throws Exception {
        LoginRequestDto request = new LoginRequestDto("login", "");

        mockMvc.perform(post("/api/users/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
