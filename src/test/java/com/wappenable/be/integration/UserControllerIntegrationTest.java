package com.wappenable.be.integration;

import com.wappenable.be.global.security.auth.CustomUserDetails;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "admin.email=testAdmin",
    "admin.password=testpass123"
})
@Transactional
@SpringBootTest
public class UserControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setup() {
        User user = User.builder()
                .email("testuser")
                .recoveryEmail("recovery@example.com")
                .nickname("통합유저")
                .passwordHash("encodedPassword")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

         // CustomUserDetails를 SecurityContext에 수동으로 설정
        CustomUserDetails userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    @WithMockUser(username = "testuser", roles = "USER")
    void 내정보조회_성공() throws Exception {
        mockMvc.perform(get("/api/users/me")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("testuser"))
            .andExpect(jsonPath("$.role").value("USER"));
    }
}
