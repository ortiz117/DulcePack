package com.dulcepack.backend.cart;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.dulcepack.backend.security.TokenService;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = "app.security.max-cart-requests=3")
class CartAbuseAndCsrfIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired TokenService tokens;

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
    void csrfCookieIsAcceptedAsHeaderAndRejectedWithoutIt() throws Exception {
        insertProduct("box-fiesta", new BigDecimal("100.00"), 20);
        MvcResult boot = mockMvc.perform(get("/api/cart")).andExpect(status().isOk()).andReturn();
        Cookie xsrf = boot.getResponse().getCookie("XSRF-TOKEN");
        org.assertj.core.api.Assertions.assertThat(xsrf).isNotNull();

        mockMvc.perform(post("/api/cart/items")
                        .cookie(xsrf)
                        .header("X-XSRF-TOKEN", xsrf.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productSlug\":\"box-fiesta\",\"quantity\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemCount").value(1));

        mockMvc.perform(patch("/api/cart/items/box-fiesta")
                        .cookie(xsrf)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void rateLimitCoversPatchDeleteAndCouponMutations() throws Exception {
        insertProduct("box-fiesta", new BigDecimal("100.00"), 50);
        Cookie cart = add("198.51.100.10").getResponse().getCookie("DP_CART");
        assertThirdRequestIsLimited(patch("/api/cart/items/box-fiesta").cookie(cart)
                .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\":2}"), "198.51.100.11");

        cart = add("198.51.100.20").getResponse().getCookie("DP_CART");
        assertThirdRequestIsLimited(delete("/api/cart/items/box-fiesta").cookie(cart), "198.51.100.21");

        cart = add("198.51.100.30").getResponse().getCookie("DP_CART");
        assertThirdRequestIsLimited(post("/api/cart/coupon").cookie(cart)
                .contentType(MediaType.APPLICATION_JSON).content("{\"code\":\"DULCE50\"}"), "198.51.100.31");

        cart = add("198.51.100.40").getResponse().getCookie("DP_CART");
        assertThirdRequestIsLimited(delete("/api/cart/coupon").cookie(cart), "198.51.100.41");
    }

    @Test
    void twoUsersLoggingInSuccessivelyOnTheSameBrowserKeepTheCookieCartIsolated() throws Exception {
        insertProduct("box-fiesta", new BigDecimal("100.00"), 20);
        register("uno@example.com");
        register("dos@example.com");
        Cookie cart = add("198.51.100.50").getResponse().getCookie("DP_CART");
        Cookie first = login("uno@example.com").getResponse().getCookie("DP_SESSION");
        Cookie second = login("dos@example.com").getResponse().getCookie("DP_SESSION");

        mockMvc.perform(get("/api/cart").cookie(cart, second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1));
        mockMvc.perform(get("/api/cart").cookie(second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(0));
        mockMvc.perform(post("/api/auth/logout").cookie(cart, second).with(csrf()))
                .andExpect(status().isNoContent());

        Cookie firstAgain = login("uno@example.com").getResponse().getCookie("DP_SESSION");
        mockMvc.perform(get("/api/cart").cookie(cart, first, firstAgain))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1));
    }

    @Test
    void expiredReservationCannotBeReactivatedByChangingQuantity() throws Exception {
        insertProduct("box-fiesta", new BigDecimal("100.00"), 1);
        Cookie stale = add("198.51.100.60").getResponse().getCookie("DP_CART");
        jdbcTemplate.update("update carts set updated_at = ? where token_hash = ?",
                Timestamp.from(Instant.now().minus(31, ChronoUnit.MINUTES)),
                tokens.hash(stale.getValue()));
        add("198.51.100.61");

        mockMvc.perform(patch("/api/cart/items/box-fiesta")
                        .cookie(stale)
                        .header("X-Forwarded-For", "198.51.100.62")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":2}")
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        mockMvc.perform(get("/api/cart").cookie(stale))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(1));
    }

    private void assertThirdRequestIsLimited(org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder builder,
            String origin) throws Exception {
        for (int index = 0; index < 3; index++) {
            mockMvc.perform(builder.header("X-Forwarded-For", origin).with(csrf()))
                    .andExpect(result -> org.assertj.core.api.Assertions
                            .assertThat(result.getResponse().getStatus()).isNotEqualTo(429));
        }
        mockMvc.perform(builder.header("X-Forwarded-For", origin).with(csrf()))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("CART_RATE_LIMITED"));
    }

    private MvcResult add(String origin) throws Exception {
        return mockMvc.perform(post("/api/cart/items")
                        .header("X-Forwarded-For", origin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productSlug\":\"box-fiesta\",\"quantity\":1}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password","displayName":"Cliente"}
                                """.formatted(email))
                        .with(csrf()))
                .andExpect(status().isCreated());
    }

    private MvcResult login(String email) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password"}
                                """.formatted(email))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();
    }

    private void insertProduct(String slug, BigDecimal price, int available) {
        UUID productId = UUID.randomUUID();
        UUID categoryId = jdbcTemplate.queryForObject(
                "select id from product_categories where slug = 'dulces'", UUID.class);
        Timestamp now = Timestamp.from(Instant.now());
        jdbcTemplate.update("""
                insert into products(id,slug,name,short_name,description,search_text,price,currency,category_id,flavor,tags,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, productId, slug, "Box Fiesta", "Box Fiesta", "Demo", "demo " + slug, price, "MXN",
                categoryId, null, "[]", now, now);
        jdbcTemplate.update("""
                insert into product_inventory(product_id,available,minimum_stock,version,updated_at)
                values (?,?,?,?,?)
                """, productId, available, 0, 0L, now);
    }
}
