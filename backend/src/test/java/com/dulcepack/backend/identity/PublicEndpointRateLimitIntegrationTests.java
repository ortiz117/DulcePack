package com.dulcepack.backend.identity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
        "app.security.max-registration-requests=2",
        "app.security.max-password-reset-requests=2",
        "app.security.max-origin-password-reset-requests=100"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PublicEndpointRateLimitIntegrationTests {

    @Autowired MockMvc mockMvc;

    @Test
    void registrationIsLimitedPerOrigin() throws Exception {
        for (int index = 0; index < 2; index++) {
            mockMvc.perform(post("/api/auth/register")
                            .header("X-Forwarded-For", "203.0.113.120")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"registration-%d@example.com","password":"Str0ng!Password","displayName":"Cliente"}
                                    """.formatted(index)))
                    .andExpect(status().isCreated());
        }

        mockMvc.perform(post("/api/auth/register")
                        .header("X-Forwarded-For", "203.0.113.120")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"registration-limited@example.com","password":"Str0ng!Password","displayName":"Cliente"}
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("REGISTRATION_RATE_LIMITED"));
    }

    @Test
    void forgotPasswordIsAccountLimitedWithoutRevealingRegistration() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .header("X-Forwarded-For", "198.51.100.121")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"known-reset@example.com","password":"Str0ng!Password","displayName":"Cliente"}
                                """))
                .andExpect(status().isCreated());

        var known = mockMvc.perform(post("/api/auth/forgot-password")
                        .header("X-Forwarded-For", "198.51.100.122")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"known-reset@example.com\"}"))
                .andExpect(status().isAccepted())
                .andReturn();
        var unknown = mockMvc.perform(post("/api/auth/forgot-password")
                        .header("X-Forwarded-For", "198.51.100.123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"unknown-reset@example.com\"}"))
                .andExpect(status().isAccepted())
                .andReturn();
        org.junit.jupiter.api.Assertions.assertEquals(
                known.getResponse().getContentAsString(), unknown.getResponse().getContentAsString());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .header("X-Forwarded-For", "198.51.100.124")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"known-reset@example.com\"}"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/api/auth/forgot-password")
                        .header("X-Forwarded-For", "198.51.100.125")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"known-reset@example.com\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("PASSWORD_RESET_RATE_LIMITED"));
    }
}
