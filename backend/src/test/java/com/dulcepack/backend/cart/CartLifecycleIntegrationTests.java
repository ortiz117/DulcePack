package com.dulcepack.backend.cart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;
import com.dulcepack.backend.security.TokenService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.max-cart-requests=10")
class CartLifecycleIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.update("delete from cart_items");
        jdbcTemplate.update("delete from carts");
        jdbcTemplate.update("delete from login_attempts");
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
        jdbcTemplate.update("delete from user_sessions");
        jdbcTemplate.update("delete from users");
    }

    @Test
    void realLoginAndLogoutPreserveTheAnonymousCartWithoutExposingItToSessions() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 20);
        Cookie cart = add("box-fiesta", 2).getResponse().getCookie("DP_CART");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cliente-carrito@example.com","password":"Str0ng!Password","displayName":"Cliente"}
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"cliente-carrito@example.com","password":"Str0ng!Password"}
                                """)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
        Cookie session = login.getResponse().getCookie("DP_SESSION");

        mockMvc.perform(get("/api/cart").cookie(cart, session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2));

        mockMvc.perform(post("/api/auth/logout").cookie(cart, session).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart").cookie(cart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(2));
        mockMvc.perform(get("/api/cart").cookie(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void reservationExpiresBeforeTheCartAndDoesNotBlockAnotherClient() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 1);
        Cookie first = add("box-fiesta", 1).getResponse().getCookie("DP_CART");
        jdbcTemplate.update("update carts set updated_at = ? where token_hash = ?",
                Timestamp.from(Instant.now().minus(31, ChronoUnit.MINUTES)),
                hash(first.getValue()));

        mockMvc.perform(get("/api/cart").cookie(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":1}
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemCount").value(1));

        jdbcTemplate.update("update carts set expires_at = ? where token_hash = ?",
                Timestamp.from(Instant.now().minusSeconds(60)), hash(first.getValue()));
        mockMvc.perform(get("/api/cart").cookie(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));
    }

    @Test
    void cartMutationsAreRateLimitedPerOrigin() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 2000);
        String origin = "203.0.113.44";
        for (int index = 0; index < 10; index++) {
            mockMvc.perform(post("/api/cart/items")
                            .header("X-Forwarded-For", origin)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"productSlug":"box-fiesta","quantity":1}
                                    """)
                            .with(csrf()))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/cart/items")
                        .header("X-Forwarded-For", origin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":1}
                                """)
                        .with(csrf()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("CART_RATE_LIMITED"));
    }

    private MvcResult add(String slug, int quantity) throws Exception {
        return mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"%s","quantity":%d}
                                """.formatted(slug, quantity))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private String hash(String value) {
        return tokenService.hash(value);
    }

    @Autowired TokenService tokenService;

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
