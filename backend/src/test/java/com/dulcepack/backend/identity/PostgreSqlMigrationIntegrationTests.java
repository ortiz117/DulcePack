package com.dulcepack.backend.identity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.sql.DriverManager;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers(disabledWithoutDocker = true)
class PostgreSqlMigrationIntegrationTests {

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("dulcepack")
            .withUsername("dulcepack")
            .withPassword("dulcepack");

    @Test
    void flywayCreatesIdentityTablesOnPostgreSql() throws Exception {
        Flyway.configure()
                .dataSource(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())
                .locations("classpath:db/migration")
                .load()
                .migrate();

        try (var connection = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());
             var statement = connection.prepareStatement("""
                     select count(*)
                     from information_schema.tables
                     where table_schema = 'public'
                       and table_name in ('users','user_sessions','password_reset_tokens','login_attempts')
                     """);
             var result = statement.executeQuery()) {
            result.next();
            assertEquals(4, result.getInt(1));
        }
    }
}
