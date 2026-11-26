package com.dulcepack.backend.identity.service;

import java.time.Clock;
import java.util.regex.Pattern;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.dulcepack.backend.identity.domain.UserEntity;
import com.dulcepack.backend.identity.domain.UserRole;
import com.dulcepack.backend.identity.repository.UserRepository;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Pattern STRONG_PASSWORD = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{12,72}$");

    private final AdminBootstrapProperties properties;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public AdminBootstrap(AdminBootstrapProperties properties, UserRepository users,
            PasswordEncoder passwordEncoder, Clock clock) {
        this.properties = properties;
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (isBlank(properties.email()) && isBlank(properties.password())) {
            return;
        }
        if (isBlank(properties.email()) || isBlank(properties.password())) {
            throw new IllegalStateException("ADMIN_EMAIL and ADMIN_PASSWORD must be configured together");
        }
        if (!STRONG_PASSWORD.matcher(properties.password()).matches()) {
            throw new IllegalStateException("ADMIN_PASSWORD does not meet the password policy");
        }
        String email = EmailNormalizer.normalize(properties.email());
        users.findByEmail(email).ifPresentOrElse(existing -> {
            if (existing.getRole() != UserRole.ADMIN) {
                throw new IllegalStateException("ADMIN_EMAIL belongs to a non-admin account");
            }
        }, () -> users.save(UserEntity.administrator(email,
                isBlank(properties.displayName()) ? "Administrador" : properties.displayName().strip(),
                passwordEncoder.encode(properties.password()), clock.instant())));
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
