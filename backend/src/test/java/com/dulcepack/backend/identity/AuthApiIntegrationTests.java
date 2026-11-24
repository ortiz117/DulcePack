package com.dulcepack.backend.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthApiIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void registerCreatesClientAccount() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "client@example.com",
                                  "password": "Str0ng!Password",
                                  "displayName": "Cliente DulcePack"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user.email").value("client@example.com"))
                .andExpect(jsonPath("$.user.role").value("CLIENT"))
                .andExpect(jsonPath("$.user.status").value("ACTIVE"));
    }

    @Test
    void loginCreatesCookieSessionAndMeReturnsCurrentUser() throws Exception {
        register("login@example.com");

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"login@example.com","password":"Str0ng!Password"}
                                """))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("DP_SESSION", true))
                .andExpect(cookie().httpOnly("DP_REFRESH", true))
                .andReturn();

        Cookie session = login.getResponse().getCookie("DP_SESSION");
        mockMvc.perform(get("/api/users/me").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("login@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void loginRejectsWrongPassword() throws Exception {
        register("wrong-password@example.com");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"wrong-password@example.com","password":"Wrong!Password1"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
    }

    @Test
    void refreshRotatesTokensAndRejectsReusedRefreshToken() throws Exception {
        register("refresh@example.com");
        MvcResult login = login("refresh@example.com");
        Cookie oldRefresh = login.getResponse().getCookie("DP_REFRESH");

        MvcResult refreshed = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(oldRefresh)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("DP_SESSION"))
                .andExpect(cookie().exists("DP_REFRESH"))
                .andReturn();

        Cookie newRefresh = refreshed.getResponse().getCookie("DP_REFRESH");
        org.junit.jupiter.api.Assertions.assertNotEquals(oldRefresh.getValue(), newRefresh.getValue());

        mockMvc.perform(post("/api/auth/refresh").cookie(oldRefresh).with(csrf()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_REFRESH_TOKEN"));
    }

    @Test
    void logoutRevokesSession() throws Exception {
        register("logout@example.com");
        MvcResult login = login("logout@example.com");
        Cookie session = login.getResponse().getCookie("DP_SESSION");
        Cookie refresh = login.getResponse().getCookie("DP_REFRESH");

        mockMvc.perform(post("/api/auth/logout").cookie(session, refresh).with(csrf()))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("DP_SESSION", 0))
                .andExpect(cookie().maxAge("DP_REFRESH", 0));

        mockMvc.perform(get("/api/users/me").cookie(session))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registrationValidatesInputAndRejectsDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid\",\"password\":\"weak\",\"displayName\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.password").exists());

        register("duplicate@example.com");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"duplicate@example.com","password":"Str0ng!Password","displayName":"Otro"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void refreshRequiresCsrfProtection() throws Exception {
        register("csrf@example.com");
        Cookie refresh = login("csrf@example.com").getResponse().getCookie("DP_REFRESH");

        mockMvc.perform(post("/api/auth/refresh").cookie(refresh))
                .andExpect(status().isForbidden());
    }

    @Test
    void repeatedBadLoginsAreRateLimited() throws Exception {
        register("limited@example.com");
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", "203.0.113.25")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"email\":\"limited@example.com\",\"password\":\"Wrong!Password1\"}"))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "203.0.113.25")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"limited@example.com\",\"password\":\"Wrong!Password1\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LOGIN_RATE_LIMITED"));
    }

    private MvcResult login(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password","displayName":"Cliente"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }
}
