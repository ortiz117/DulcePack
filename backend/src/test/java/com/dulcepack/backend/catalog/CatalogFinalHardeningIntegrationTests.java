package com.dulcepack.backend.catalog;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogFinalHardeningIntegrationTests {

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
    void rejectsPngWithValidSignatureButUndecodableStructure() throws Exception {
        Cookie admin = adminSession("estructura-png@example.com");
        byte[] truncated = new byte[] {
                (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n',
                0x00, 0x00, 0x00, 0x0d, 'I', 'H', 'D', 'R'
        };

        mockMvc.perform(multipart("/api/admin/products")
                        .file(jsonPart("Producto Falso", 1, 0))
                        .file(new MockMultipartFile("image", "falso.png", "image/png", truncated))
                        .cookie(admin).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_IMAGE"));
    }

    @Test
    void rejectsJpegBytesDeclaredAsPng() throws Exception {
        Cookie admin = adminSession("estructura-jpeg@example.com");

        mockMvc.perform(multipart("/api/admin/products")
                        .file(jsonPart("Producto Mixto", 1, 0))
                        .file(new MockMultipartFile("image", "mixto.png", "image/png", TestImages.jpeg()))
                        .cookie(admin).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_IMAGE"));
    }

    @Test
    void acceptsDecodablePngJpegAndGifImages() throws Exception {
        Cookie admin = adminSession("estructura-validas@example.com");
        String[] names = {"Imagen PNG", "Imagen JPEG", "Imagen GIF"};
        String[] types = {"image/png", "image/jpeg", "image/gif"};
        byte[][] contents = {TestImages.png(), TestImages.jpeg(), TestImages.gif()};

        for (int index = 0; index < names.length; index++) {
            mockMvc.perform(multipart("/api/admin/products")
                            .file(jsonPart(names[index], 1, 0))
                            .file(new MockMultipartFile("image", "valida-" + index, types[index], contents[index]))
                            .cookie(admin).with(csrf()))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void unpagedCatalogIsCappedAtOneHundredProducts() throws Exception {
        insertProducts(101);

        mockMvc.perform(get("/api/catalog/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100));
    }

    @Test
    void adminListIsCappedAtOneHundredProducts() throws Exception {
        Cookie admin = adminSession("limite-admin@example.com");
        insertProducts(101);

        mockMvc.perform(get("/api/admin/products").cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(100));
    }

    @Test
    void pagedEndpointCanReachProductsBeyondTheUnpagedCap() throws Exception {
        insertProducts(101);

        mockMvc.perform(get("/api/catalog/products/page").param("page", "1").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(101));
    }

    private MockMultipartFile jsonPart(String name, int stock, int minimum) {
        return new MockMultipartFile("product", "product.json", MediaType.APPLICATION_JSON_VALUE,
                ("""
                {"name":"%s","description":"Demo","price":10,"stock":%d,"minimumStock":%d,
                 "category":"Dulces","tags":[]}
                """.formatted(name, stock, minimum)).getBytes());
    }

    private void insertProducts(int count) {
        UUID categoryId = jdbcTemplate.queryForObject(
                "select id from product_categories where slug = 'dulces'", UUID.class);
        Instant now = Instant.now();
        for (int index = 0; index < count; index++) {
            UUID productId = UUID.randomUUID();
            String slug = "limite-%03d".formatted(index);
            jdbcTemplate.update("""
                    insert into products(id,slug,name,short_name,description,search_text,price,currency,category_id,flavor,tags,created_at,updated_at)
                    values (?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """, productId, slug, "Producto " + index, "Producto " + index, "Demo",
                    "demo producto " + index, new BigDecimal("10.00"), "MXN", categoryId, null, "[]",
                    Timestamp.from(now), Timestamp.from(now));
            jdbcTemplate.update("""
                    insert into product_inventory(product_id,available,minimum_stock,version,updated_at)
                    values (?,?,?,?,?)
                    """, productId, 5, 1, 0L, Timestamp.from(now));
        }
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
