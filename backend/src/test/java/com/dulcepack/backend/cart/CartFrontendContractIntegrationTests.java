package com.dulcepack.backend.cart;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CartFrontendContractIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.update("delete from cart_items");
        jdbcTemplate.update("delete from carts");
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
    }

    @Test
    void cartResponseContainsEveryFieldReadByTheFrontendAdapter() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        String adapter = Files.readString(Path.of("..", "frontend", "src", "lib", "api", "cart.ts"));
        for (String field : new String[] {"items", "itemCount", "subtotal", "shipping", "discount",
                "total", "couponCode"}) {
            assertTrue(adapter.contains(field), "Adapter is missing " + field);
        }
        for (String field : new String[] {"productSlug", "name", "unitPrice", "quantity", "lineTotal", "available"}) {
            assertTrue(adapter.contains(field), "CartItem is missing " + field);
        }

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":1}
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie cookie = result.getResponse().getCookie("DP_CART");

        mockMvc.perform(get("/api/cart").cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].productSlug").value("box-fiesta"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(100.0))
                .andExpect(jsonPath("$.items[0].lineTotal").value(100.0))
                .andExpect(jsonPath("$.itemCount").value(1))
                .andExpect(jsonPath("$.subtotal").value(100.0))
                .andExpect(jsonPath("$.shipping").value(79.0))
                .andExpect(jsonPath("$.discount").value(50.0))
                .andExpect(jsonPath("$.total").value(129.0))
                .andExpect(jsonPath("$.couponCode").value("DULCE50"));
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
