package com.berk.lab10.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for CSRF Protection
 *
 * IMPORTANT for this project:
 * - Missing CSRF does NOT return 403 directly; it redirects to /access-denied (302).
 * - Some "error" situations are rendered as HTML error pages but returned with HTTP 200 by the app.
 *   So for "csrf passed" we assert: NOT redirected to /access-denied, and accept 200/3xx/404.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CsrfIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ---------- Helpers ----------

    private void expectAccessDeniedRedirect(MvcResult result) {
        int status = result.getResponse().getStatus();
        String location = result.getResponse().getRedirectedUrl();

        Assertions.assertTrue(status >= 300 && status < 400, "Expected 3xx redirect but was: " + status);
        Assertions.assertEquals("/access-denied", location, "Expected redirect to /access-denied but was: " + location);
    }

    /**
     * CSRF passed means: request must NOT be redirected to /access-denied.
     * Accepts: 200 OK, 3xx redirect (to anywhere except /access-denied), or 404.
     */
    private void expectCsrfPassed(MvcResult result) throws Exception {
        int status = result.getResponse().getStatus();
        String location = result.getResponse().getRedirectedUrl();

        if (status >= 300 && status < 400) {
            // It redirected, but must not be access-denied
            Assertions.assertNotEquals("/access-denied", location, "CSRF should pass but redirected to /access-denied");
            return;
        }

        // Some controllers render error pages as 200. Also some may return 404.
        Assertions.assertTrue(
                status == 200 || status == 404,
                "Expected 200 or 404 when CSRF passes, but was: " + status
        );
    }

    private void expectOkErrorPageOr404(MvcResult result) throws Exception {
        int status = result.getResponse().getStatus();
        if (status == 404) return;

        // If app returns error view with 200, at least confirm it's an error page
        Assertions.assertEquals(200, status, "Expected 200 error page or 404, but was: " + status);
        String body = result.getResponse().getContentAsString();
        Assertions.assertTrue(
                body.contains("Error") || body.contains("Internal Server Error"),
                "Expected an error HTML page body, but got:\n" + body
        );
    }

    // ============ REGISTER ENDPOINT ============

    @Test
    void register_WithoutCsrf_ShouldRedirectToAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/register")
                        .param("username", "newuser")
                        .param("email", "new@example.com")
                        .param("password", "StrongPass1!")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();

        expectAccessDeniedRedirect(result);
    }

    @Test
    void register_WithCsrf_ShouldNotBeAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/register")
                        .param("username", "newuser")
                        .param("email", "newuser@example.com")
                        .param("password", "StrongPass1!")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andReturn();

        // could redirect to /login OR /register?error=exists depending on DB state
        expectCsrfPassed(result);
    }

    // ============ NOTE CREATION (AUTHENTICATED) ============

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void createNote_WithoutCsrf_ShouldRedirectToAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes")
                        .param("title", "Test Note")
                        .param("content", "Test Content")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();

        expectAccessDeniedRedirect(result);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void createNote_WithCsrf_ShouldNotBeAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes")
                        .param("title", "Test Note")
                        .param("content", "Test Content")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andReturn();

        // Your app might redirect OR return 200
        expectCsrfPassed(result);
    }

    // ============ NOTE UPDATE ============

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateNote_WithoutCsrf_ShouldRedirectToAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes/1/edit")
                        .param("title", "Updated Note")
                        .param("content", "Updated Content")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();

        expectAccessDeniedRedirect(result);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void updateNote_WithCsrf_ShouldAttemptUpdate() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes/999/edit")
                        .param("title", "Updated Note")
                        .param("content", "Updated Content")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andReturn();

        // CSRF passed; app may respond 404 or 200 error page
        expectCsrfPassed(result);
        expectOkErrorPageOr404(result);
    }

    // ============ NOTE DELETE ============

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deleteNote_WithoutCsrf_ShouldRedirectToAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes/1/delete")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();

        expectAccessDeniedRedirect(result);
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void deleteNote_WithCsrf_ShouldAttemptDelete() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes/999/delete")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andReturn();

        // CSRF passed; app may respond 404 or 200 error page
        expectCsrfPassed(result);
        expectOkErrorPageOr404(result);
    }

    // ============ LOGOUT ============

    @Test
    void logout_WithoutCsrf_ShouldRedirectToAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andReturn();

        expectAccessDeniedRedirect(result);
    }

    @Test
    void logout_WithCsrf_ShouldNotBeAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/logout")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf()))
                .andReturn();

        // your app redirects to /login?logout=true (or could be 200 depending on config)
        expectCsrfPassed(result);
    }

    // ============ EDGE CASES ============

    @Test
    void csrfProtection_AppliesToAllPOSTRequests() throws Exception {
        String[] postEndpoints = {"/register", "/logout"};

        for (String endpoint : postEndpoints) {
            MvcResult result = mockMvc.perform(post(endpoint)).andReturn();
            expectAccessDeniedRedirect(result);
        }
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = "USER")
    void csrfProtection_WithEmptyToken_ShouldRedirectToAccessDenied() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/notes")
                        .param("title", "Test")
                        .param("content", "Test"))
                .andReturn();

        expectAccessDeniedRedirect(result);
    }
}
