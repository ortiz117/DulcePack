package com.dulcepack.backend.identity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.dulcepack.backend.identity.repository.UserRepository;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminUserIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired UserRepository users;

    @Test
    void clientCannotAccessAdminUsers() throws Exception {
        register("ordinary@example.com");
        Cookie session = login("ordinary@example.com").getResponse().getCookie("DP_SESSION");

        mockMvc.perform(get("/api/admin/users").cookie(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminListsUsersAndSuspensionRevokesSessionsAndBlocksLogin() throws Exception {
        createAdmin("admin@example.com");
        register("suspended@example.com");
        Cookie clientSession = login("suspended@example.com").getResponse().getCookie("DP_SESSION");
        Cookie adminSession = login("admin@example.com").getResponse().getCookie("DP_SESSION");
        UUID clientId = users.findByEmail("suspended@example.com").orElseThrow().getId();

        mockMvc.perform(get("/api/admin/users").cookie(adminSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.email == 'suspended@example.com')]").exists());

        mockMvc.perform(patch("/api/admin/users/{id}/status", clientId)
                        .cookie(adminSession)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SUSPENDED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUSPENDED"));

        mockMvc.perform(get("/api/users/me").cookie(clientSession))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"suspended@example.com\",\"password\":\"Str0ng!Password\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("ACCOUNT_SUSPENDED"));
    }

    private void createAdmin(String email) {
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into users(id,email,display_name,password_hash,role,status,password_changed_at,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?)
                """, UUID.randomUUID(), email, "Administrador", passwordEncoder.encode("Str0ng!Password"),
                "ADMIN", "ACTIVE", Timestamp.from(now), Timestamp.from(now), Timestamp.from(now));
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password","displayName":"Cliente"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
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
}
