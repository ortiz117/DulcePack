package com.dulcepack.backend.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.dulcepack.backend.identity.domain.PasswordResetTokenEntity;
import com.dulcepack.backend.identity.domain.UserEntity;
import com.dulcepack.backend.identity.repository.PasswordResetTokenRepository;
import com.dulcepack.backend.identity.repository.UserRepository;
import com.dulcepack.backend.security.TokenService;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class PasswordResetConcurrencyPostgreSqlIntegrationTests {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("dulcepack")
            .withUsername("dulcepack")
            .withPassword("dulcepack");

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired UserRepository users;
    @Autowired PasswordResetTokenRepository resetTokens;
    @Autowired TokenService tokenService;
    @Autowired Clock clock;
    @Autowired TransactionTemplate transactions;

    @Test
    void resetTokenCanBeConsumedOnlyOnceUnderConcurrency() throws Exception {
        var now = clock.instant();
        var user = users.save(UserEntity.client("concurrent-reset@example.com", "Cliente", "hash", now));
        String rawToken = tokenService.generate();
        String tokenHash = tokenService.hash(rawToken);
        resetTokens.save(PasswordResetTokenEntity.create(user, tokenHash, now.plus(Duration.ofMinutes(30)), now));

        var barrier = new CyclicBarrier(2);
        Callable<Integer> consume = () -> {
            barrier.await();
            return transactions.execute(status -> resetTokens.consumeIfValid(tokenHash, clock.instant()));
        };

        try (var executor = Executors.newFixedThreadPool(2)) {
            var results = executor.invokeAll(List.of(consume, consume));
            int consumed = results.get(0).get() + results.get(1).get();
            assertEquals(1, consumed);
        }
    }
}
