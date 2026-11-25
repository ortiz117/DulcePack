package com.dulcepack.backend.catalog;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CatalogPostgreSqlIntegrationTests {

    private static final byte[] PNG = TestImages.png();

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

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void clearCatalog() {
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
    }

    @Test
    void createAndUpdateProductWithImageWorkOnPostgreSql() throws Exception {
        Cookie admin = adminSession("catalog-pg-create@example.com");
        MockMultipartFile product = jsonPart("""
                {"name":"Producto Postgres","description":"Imagen y stock reales","price":99.50,
                 "stock":4,"minimumStock":6,"category":"Dulces","tags":["Postgres"],"flavor":"dulce"}
                """);
        MockMultipartFile image = new MockMultipartFile("image", "postgres.png", "image/png", PNG);

        mockMvc.perform(multipart("/api/admin/products").file(product).file(image)
                        .cookie(admin).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("producto-postgres"))
                .andExpect(jsonPath("$.minimumStock").value(6))
                .andExpect(jsonPath("$.status").value("LOW"));

        mockMvc.perform(get("/api/catalog/products/producto-postgres/image"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", "sandbox"));

        MockMultipartFile update = jsonPart("""
                {"name":"Producto Postgres Editado","description":"Actualizado","price":109.50,
                 "stock":12,"minimumStock":3,"category":"Paquetes","tags":["Editado"],"flavor":"picante"}
                """);
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/products/producto-postgres")
                        .file(update).cookie(admin).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Producto Postgres Editado"))
                .andExpect(jsonPath("$.stock").value(12))
                .andExpect(jsonPath("$.minimumStock").value(3))
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void searchEscapesWildcardsAndPaginatesOnPostgreSql() throws Exception {
        insertProduct("pg-uno", "Producto Uno PG");
        insertProduct("pg-dos", "Producto Dos PG");

        mockMvc.perform(get("/api/catalog/products").param("query", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/catalog/products/page").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void deleteRemovesProductAndChildrenOnPostgreSql() throws Exception {
        Cookie admin = adminSession("catalog-pg-delete@example.com");
        MockMultipartFile product = jsonPart("""
                {"name":"Producto temporal PG","description":"Demo","price":10,
                 "stock":1,"category":"Botanas","tags":[]}
                """);
        MockMultipartFile image = new MockMultipartFile("image", "temporal.png", "image/png", PNG);
        mockMvc.perform(multipart("/api/admin/products").file(product).file(image)
                        .cookie(admin).with(csrf()))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/admin/products/producto-temporal-pg").cookie(admin).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/catalog/products/producto-temporal-pg"))
                .andExpect(status().isNotFound());
    }

    private void insertProduct(String slug, String name) {
        UUID productId = UUID.randomUUID();
        UUID categoryId = jdbcTemplate.queryForObject(
                "select id from product_categories where slug = 'dulces'", UUID.class);
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into products(id,slug,name,short_name,description,search_text,price,currency,category_id,flavor,tags,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, productId, slug, name, name, "Demo", "demo " + name.toLowerCase(),
                new java.math.BigDecimal("10.00"), "MXN", categoryId, null, "[]",
                Timestamp.from(now), Timestamp.from(now));
        jdbcTemplate.update("""
                insert into product_inventory(product_id,available,minimum_stock,version,updated_at)
                values (?,?,?,?,?)
                """, productId, 5, 1, 0L, Timestamp.from(now));
    }

    private MockMultipartFile jsonPart(String json) {
        return new MockMultipartFile("product", "product.json", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes());
    }

    private Cookie adminSession(String email) throws Exception {
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into users(id,email,display_name,password_hash,role,status,password_changed_at,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?)
                """, UUID.randomUUID(), email, "Administrador", passwordEncoder.encode("Str0ng!Password"),
                "ADMIN", "ACTIVE", Timestamp.from(now), Timestamp.from(now), Timestamp.from(now));
        return login(email);
    }

    private Cookie login(String email) throws Exception {
        MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password"}
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getCookie("DP_SESSION");
    }
}
