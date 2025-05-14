package com.wappenable.be.users.controller;

import com.wappenable.be.users.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
