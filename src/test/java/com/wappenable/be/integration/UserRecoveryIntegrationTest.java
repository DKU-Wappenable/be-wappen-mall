package com.wappenable.be.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.request.FindPasswordRequestDto;
import com.wappenable.be.users.dto.request.ResetPasswordRequestDto;
import com.wappenable.be.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "admin.email=testAdmin",
    "admin.password=testpass123"
})
@Transactional
public class UserRecoveryIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .email("recovery")
                .recoveryEmail("recovery@email.com")
                .nickname("복구유저")
                .passwordHash(passwordEncoder.encode("originalPass123!"))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
    }

    @Test
    @DisplayName("아이디 찾기 성공")
    void find_email_success() throws Exception {
        mockMvc.perform(post("/api/users/find-id")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"recoveryEmail\": \"recovery@email.com\"}"))
            .andExpect(status().isOk())
            .andExpect(content().string("recovery"));
    }

    @Test
    @DisplayName("비밀번호 초기화 성공 - 임시 비밀번호 반환")
    void reset_temp_password_success() throws Exception {
        FindPasswordRequestDto request = new FindPasswordRequestDto("recovery", "recovery@email.com");

        mockMvc.perform(post("/api/users/find-pw")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string(org.hamcrest.Matchers.hasLength(10)));
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    @WithMockUser(username = "recovery", roles = "USER")
    void reset_password_success() throws Exception {
        ResetPasswordRequestDto request = new ResetPasswordRequestDto(
                "recovery",
                "Newpass123!"
        );

        mockMvc.perform(post("/api/users/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().string("비밀번호가 성공적으로 변경되었습니다."));
    }
}