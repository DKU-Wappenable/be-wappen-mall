package com.wappenable.be.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wappenable.be.users.dto.request.SignupRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
import com.wappenable.be.users.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;

@SpringBootTest
@TestPropertySource(properties = {
    "admin.email=test-admin@wappen.com",
    "admin.password=testpass123"
})
@AutoConfigureMockMvc
@Transactional
class UserSignupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("회원가입 성공 - DB 저장 확인")
    void signup_success() throws Exception {
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("test@example.com");
        request.setNickname("tester");
        request.setPassword("Newpass123!");
        request.setConfirmPassword("Newpass123!");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk());

        User savedUser = userRepository.findByEmail("test@example.com").orElse(null);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getNickname()).isEqualTo("tester");
    }

    @Test
    @DisplayName("중복 이메일로 회원가입 실패 - DB 저장 안됨")
    void signup_duplicateEmail() throws Exception {
        User existingUser = User.builder()
                .email("duplicate@example.com")
                .nickname("dup")
                .passwordHash("password123")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(existingUser);

        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("duplicate@example.com");
        request.setNickname("newdup");
        request.setPassword("Newpass123!");
        request.setConfirmPassword("Newpass123!");
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isConflict());

        long count = userRepository.findAll().stream()
            .filter(u -> u.getEmail().equals("duplicate@example.com"))
            .count();
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("회원가입 실패 - 비밀번호와 비밀번호 확인 불일치")
    void signup_passwordMismatch() throws Exception {
        SignupRequestDto request = new SignupRequestDto();
        request.setEmail("mismatch@example.com");
        request.setNickname("MismatchUser");
        request.setPassword("Newpass123!");
        request.setConfirmPassword("Wrongpass123!"); // 비밀번호 확인 불일치
        request.setRole(Role.USER);

        mockMvc.perform(post("/api/users/signup")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isBadRequest()); // UserService에서 HttpStatus.BAD_REQUEST 반환
    }
}
