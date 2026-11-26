package com.dulcepack.backend.cart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.dulcepack.backend.security.TokenService;
import com.dulcepack.backend.shared.error.ApiException;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class CartPostgreSqlIntegrationTests {

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
    @Autowired CartService cartService;
    @Autowired TokenService tokenService;

    @BeforeEach
    void resetCatalogAndCarts() {
        jdbcTemplate.update("delete from cart_items");
        jdbcTemplate.update("delete from carts");
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
    }

    @Test
    void migrationCreatesCartTablesAndSeedsTheCoupon() {
        Integer tables = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name in ('carts','cart_items','promotions')",
                Integer.class);
        assertEquals(3, tables);
        BigDecimal discount = jdbcTemplate.queryForObject(
                "select discount_amount from promotions where code = 'DULCE50'", BigDecimal.class);
        assertEquals(0, new BigDecimal("50.00").compareTo(discount));
    }

    @Test
    void priceChangesInTheCatalogAreReflectedInTheCart() throws Exception {
        insertProduct("producto-pg", "Producto PG", new BigDecimal("100.00"), 5);
        var response = mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"producto-pg","quantity":1}
                                """)
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subtotal").value(100.0))
                .andReturn();
        var cookie = response.getResponse().getCookie("DP_CART");

        jdbcTemplate.update("update products set price = 125.00 where slug = 'producto-pg'");

        mockMvc.perform(get("/api/cart").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].unitPrice").value(125.0))
                .andExpect(jsonPath("$.subtotal").value(125.0))
                .andExpect(jsonPath("$.total").value(154.0));
    }

    @Test
    void concurrentAddsDoNotExceedAvailableStock() throws Exception {
        insertProduct("producto-pg", "Producto PG", new BigDecimal("100.00"), 1);
        String firstToken = tokenService.generate();
        String secondToken = tokenService.generate();
        var barrier = new CyclicBarrier(2);
        Callable<Boolean> firstAdd = () -> {
            barrier.await();
            try {
                cartService.addItem(firstToken, "producto-pg", 1);
                return true;
            } catch (ApiException exception) {
                return false;
            }
        };
        Callable<Boolean> secondAdd = () -> {
            barrier.await();
            try {
                cartService.addItem(secondToken, "producto-pg", 1);
                return true;
            } catch (ApiException exception) {
                return false;
            }
        };
        try (var executor = Executors.newFixedThreadPool(2)) {
            var results = executor.invokeAll(List.of(firstAdd, secondAdd));
            int accepted = (results.get(0).get() ? 1 : 0) + (results.get(1).get() ? 1 : 0);
            assertEquals(1, accepted);
        }
        Integer reserved = jdbcTemplate.queryForObject(
                "select coalesce(sum(ci.quantity), 0) from cart_items ci join products p on p.id = ci.product_id where p.slug = 'producto-pg'",
                Integer.class);
        assertEquals(1, reserved);
        Integer available = jdbcTemplate.queryForObject(
                "select i.available from product_inventory i join products p on p.id = i.product_id where p.slug = 'producto-pg'",
                Integer.class);
        assertTrue(reserved <= available);
    }

    private void insertProduct(String slug, String name, BigDecimal price, int available) {
        UUID productId = UUID.randomUUID();
        UUID categoryId = jdbcTemplate.queryForObject(
                "select id from product_categories where slug = 'dulces'", UUID.class);
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into products(id,slug,name,short_name,description,search_text,price,currency,category_id,flavor,tags,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, productId, slug, name, name, "Demo", "demo " + name.toLowerCase(), price, "MXN",
                categoryId, null, "[]", Timestamp.from(now), Timestamp.from(now));
        jdbcTemplate.update("""
                insert into product_inventory(product_id,available,minimum_stock,version,updated_at)
                values (?,?,?,?,?)
                """, productId, available, 0, 0L, Timestamp.from(now));
    }
}
