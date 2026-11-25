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
class CartApiIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;
    @Autowired CartService cartService;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.update("delete from cart_items");
        jdbcTemplate.update("delete from carts");
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
    }

    @Test
    void addToCartCalculatesTotalsUsingServerPricesAndDefaultCoupon() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":2,"price":1,"discount":999,"total":1}
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productSlug").value("box-fiesta"))
                .andExpect(jsonPath("$.items[0].unitPrice").value(100.0))
                .andExpect(jsonPath("$.items[0].lineTotal").value(200.0))
                .andExpect(jsonPath("$.itemCount").value(2))
                .andExpect(jsonPath("$.subtotal").value(200.0))
                .andExpect(jsonPath("$.shipping").value(79.0))
                .andExpect(jsonPath("$.discount").value(50.0))
                .andExpect(jsonPath("$.total").value(229.0))
                .andExpect(jsonPath("$.couponCode").value("DULCE50"))
                .andReturn();

        Cookie cartCookie = result.getResponse().getCookie("DP_CART");
        org.junit.jupiter.api.Assertions.assertNotNull(cartCookie);
        org.junit.jupiter.api.Assertions.assertTrue(cartCookie.isHttpOnly());

        mockMvc.perform(get("/api/cart").cookie(cartCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(229.0));
    }

    @Test
    void addingTheSameProductTwiceAccumulatesQuantity() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        MvcResult result = add("box-fiesta", 1);
        Cookie cookie = result.getResponse().getCookie("DP_CART");

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":2}
                                """)
                        .cookie(cookie).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.itemCount").value(3))
                .andExpect(jsonPath("$.subtotal").value(300.0));
    }

    @Test
    void quantityCanBeUpdatedAndItemRemoved() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        Cookie cookie = add("box-fiesta", 1).getResponse().getCookie("DP_CART");

        mockMvc.perform(patch("/api/cart/items/box-fiesta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"quantity":3}
                                """)
                        .cookie(cookie).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount").value(3))
                .andExpect(jsonPath("$.subtotal").value(300.0));

        mockMvc.perform(delete("/api/cart/items/box-fiesta").cookie(cookie).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0))
                .andExpect(jsonPath("$.shipping").value(0.0))
                .andExpect(jsonPath("$.discount").value(0.0))
                .andExpect(jsonPath("$.total").value(0.0));
    }

    @Test
    void couponCanBeClearedAndInvalidCouponIsRejected() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        Cookie cookie = add("box-fiesta", 1).getResponse().getCookie("DP_CART");

        mockMvc.perform(delete("/api/cart/coupon").cookie(cookie).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.discount").value(0.0))
                .andExpect(jsonPath("$.total").value(179.0));

        mockMvc.perform(post("/api/cart/coupon")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"NO-EXISTE"}
                                """)
                        .cookie(cookie).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_COUPON"));

        mockMvc.perform(get("/api/cart").cookie(cookie))
                .andExpect(jsonPath("$.discount").value(0.0));
    }

    @Test
    void quantityAboveAvailableStockIsRejected() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":6}
                                """)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    void zeroOrNegativeQuantityIsRejected() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":0}
                                """)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    void cartMutationsRequireCsrfToken() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":1}
                                """))
                .andExpect(status().isForbidden());
    }


    @Test
    void cartsAreIsolatedByCookie() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        Cookie first = add("box-fiesta", 1).getResponse().getCookie("DP_CART");
        Cookie second = add("box-fiesta", 2).getResponse().getCookie("DP_CART");

        mockMvc.perform(get("/api/cart").cookie(first))
                .andExpect(jsonPath("$.itemCount").value(1));
        mockMvc.perform(get("/api/cart").cookie(second))
                .andExpect(jsonPath("$.itemCount").value(2));
    }

    @Test
    void clientSuppliedCartTokenIsNotAdopted() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        Cookie attacker = new Cookie("DP_CART", "token-elegido-por-el-cliente");

        MvcResult result = mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":1}
                                """)
                        .cookie(attacker)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andReturn();
        Cookie issued = result.getResponse().getCookie("DP_CART");
        org.junit.jupiter.api.Assertions.assertNotNull(issued);
        org.junit.jupiter.api.Assertions.assertNotEquals(attacker.getValue(), issued.getValue());
    }

    @Test
    void expiredCartReleasesItsStockReservation() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 5);
        MvcResult result = add("box-fiesta", 2);
        String token = result.getResponse().getCookie("DP_CART").getValue();
        jdbcTemplate.update("update carts set expires_at = ?", Timestamp.from(Instant.now().minusSeconds(60)));

        org.junit.jupiter.api.Assertions.assertEquals(0, cartService.getCart(token).itemCount());
        Integer reserved = jdbcTemplate.queryForObject("select coalesce(sum(quantity), 0) from cart_items",
                Integer.class);
        org.junit.jupiter.api.Assertions.assertEquals(0, reserved);
    }

    @Test
    void expiredReservationDoesNotBlockAnotherCart() throws Exception {
        insertProduct("box-fiesta", "Box Fiesta", new BigDecimal("100.00"), 1);
        add("box-fiesta", 1);
        jdbcTemplate.update("update carts set expires_at = ?", Timestamp.from(Instant.now().minusSeconds(60)));

        mockMvc.perform(post("/api/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"productSlug":"box-fiesta","quantity":1}
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.itemCount").value(1));
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
