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
        "app.security.max-login-failures=100",
        "app.security.max-origin-login-failures=3"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OriginLoginRateLimitIntegrationTests {

    @Autowired MockMvc mockMvc;

    @Test
    void oneOriginIsLimitedWhenSprayingDifferentAccounts() throws Exception {
        for (int index = 0; index < 3; index++) {
            mockMvc.perform(post("/api/auth/login")
                            .header("X-Forwarded-For", "203.0.113.99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"email":"spray-%d@example.com","password":"Wrong!Password1"}
                                    """.formatted(index)))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/auth/login")
                        .header("X-Forwarded-For", "203.0.113.99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"another@example.com\",\"password\":\"Wrong!Password1\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("LOGIN_RATE_LIMITED"));
    }
}
