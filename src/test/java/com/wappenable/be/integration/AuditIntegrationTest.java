package com.wappenable.be.integration;

import com.wappenable.be.users.dto.request.RoleUpdateRequestDto;
import com.wappenable.be.users.entity.Role;
import com.wappenable.be.users.entity.User;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@TestPropertySource(properties = {
    "admin.email=test-admin@wappen.com",
    "admin.password=testpass123"
})
@AutoConfigureMockMvc
@Transactional
class AuditIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;

    private Long userId;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .email(UUID.randomUUID() + "@audit.com")
                .nickname("Audit Test")
                .passwordHash("pw")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        userId = userRepository.save(user).getId();
    }

    @Test
    @DisplayName("권한 변경 시 updatedAt, updatedBy가 채워지는지 확인")
    @WithMockUser(username = "admin@wappen.com", roles = "ADMIN")
    void auditFieldsUpdated_onRoleChange() throws Exception {
        // when
        mockMvc.perform(put("/api/admin/users/" + userId + "/role")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\": \"ADMIN\"}"))
            .andExpect(status().isOk());

        // then
        User updated = userRepository.findById(userId).orElseThrow();
        assertThat(updated.getUpdatedAt()).isNotNull();
        assertThat(updated.getUpdatedBy()).isEqualTo("admin@wappen.com");
    }
}
