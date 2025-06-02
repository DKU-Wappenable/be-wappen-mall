package com.wappenable.be.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wappenable.be.global.exception.admin.AdminPermissionRequiredException;
import com.wappenable.be.global.exception.admin.RoleNotValidException;
import com.wappenable.be.global.exception.users.UserNotFoundException;
import com.wappenable.be.users.domain.Role;
import com.wappenable.be.users.domain.User;
import com.wappenable.be.users.dto.request.RoleUpdateRequestDto;
import com.wappenable.be.users.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@TestPropertySource(properties = {
    "admin.email=testAdmin",
    "admin.password=testpass123"
})
@AutoConfigureMockMvc
@Transactional
class AdminUserApiIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;

    private Long testUserId;


    @BeforeEach
    void setup() {
        // 테스트용 유저 저장
        User user = User.builder()
                .email(UUID.randomUUID().toString())
                .recoveryEmail(UUID.randomUUID().toString() + "@wappen.com")
                .nickname("Test User")
                .passwordHash("encrypted") // 패스워드 해시는 실제 로그인과 관계 없음
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        testUserId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("역할 변경 성공")
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_success() throws Exception {
        RoleUpdateRequestDto dto = new RoleUpdateRequestDto(Role.ADMIN);

        mockMvc.perform(put("/api/admin/users/" + testUserId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 사용자 ID → 400 BadRequest")
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_notFound() throws Exception {
        RoleUpdateRequestDto dto = new RoleUpdateRequestDto(Role.ADMIN);

        mockMvc.perform(put("/api/admin/users/999999/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 역할 문자열 → 400 BadRequest")
    @WithMockUser(roles = "ADMIN")
    void updateUserRole_invalidRole() throws Exception {
        String invalidJson = """
            {
                "role": "NOT_EXIST"
            }
        """;

        mockMvc.perform(put("/api/admin/users/" + testUserId + "/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 목록 조회 성공")
    @WithMockUser(roles = "ADMIN")
    void getUserList_success() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").isNotEmpty());
    }  

    // ADMIN이 아닌 계정이 전체 사용자 목록 조회를 시도함
    @Test
    @DisplayName("일반 사용자 목록 조회 → 403 Forbidden")
    @WithMockUser(roles = "USER")
    void getUserList_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isForbidden());

    }

    @Test
    @DisplayName("ROLE_ADMIN 권한으로 Role 드롭다운 조회 성공")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void getAvailableRoles_success() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Role.values().length))
                .andExpect(jsonPath("$[0]").value(Role.USER.name()));
    }

    @Test
    @DisplayName("ROLE_USER 권한으로 Role 드롭다운 접근 시 403 Forbidden")
    @WithMockUser(username = "user", roles = {"USER"})
    void getAvailableRoles_forbidden() throws Exception {
        mockMvc.perform(get("/api/admin/roles"))
                .andExpect(status().isForbidden());
    }
}
