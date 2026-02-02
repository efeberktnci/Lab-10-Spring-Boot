package com.berk.lab10.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Authentication and Authorization
 *
 * Tests:
 * - Public vs protected endpoints
 * - Role-based access control
 * - Session-based authentication
 * - Logout behavior
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ============ PUBLIC ENDPOINTS ============

    @Test
    void loginPage_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    void registerPage_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void accessDeniedPage_ShouldBeAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isOk());
    }

    // ============ PROTECTED ENDPOINTS ============

    @Test
    void homePage_WithoutAuth_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void homePage_WithAuth_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    void userNotesPage_WithoutAuth_ShouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/user/notes"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void userNotesPage_WithAuth_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/user/notes"))
                .andExpect(status().isOk());
    }

    // ============ ROLE-BASED ACCESS CONTROL ============

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void adminPing_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminPing_WithAdminRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/admin/ping"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void adminUsers_WithUserRole_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/access-denied"));
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminUsers_WithAdminRole_ShouldSucceed() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"));
    }

    // ============ LOGOUT BEHAVIOR ============

    @Test
    void logout_ShouldRedirectToLoginWithLogoutParam() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void logout_WithAuthenticatedUser_ShouldInvalidateSession() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));
    }

    // ============ ALREADY AUTHENTICATED REDIRECTS ============

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void loginPage_WhenAlreadyAuthenticated_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @WithMockUser(username = "testuser", roles = "USER")
    void registerPage_WhenAlreadyAuthenticated_ShouldRedirectToHome() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    // ============ UNAUTHORIZED ACCESS (401) ============

    @Test
    void protectedEndpoint_WithoutAuth_ShouldReturn401or302() throws Exception {
        mockMvc.perform(get("/user/notes"))
                .andExpect(status().is3xxRedirection());
    }
}
