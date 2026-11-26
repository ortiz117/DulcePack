package com.dulcepack.backend.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import com.dulcepack.backend.identity.domain.UserRole;
import com.dulcepack.backend.identity.repository.UserRepository;

@SpringBootTest(properties = {
        "app.bootstrap-admin.email=bootstrap-admin@example.com",
        "app.bootstrap-admin.password=Adm1n!SecurePassword",
        "app.bootstrap-admin.display-name=Administrador inicial"
})
@ActiveProfiles("test")
class AdminBootstrapIntegrationTests {

    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void configuredAdministratorIsCreatedWithHashedPassword() {
        var admin = users.findByEmail("bootstrap-admin@example.com").orElseThrow();
        assertEquals(UserRole.ADMIN, admin.getRole());
        assertTrue(passwordEncoder.matches("Adm1n!SecurePassword", admin.getPasswordHash()));
    }
}
