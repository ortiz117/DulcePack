package com.dulcepack.backend.catalog;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogFrontendContractIntegrationTests {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearCatalog() {
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
    }

    @Test
    void frontendCategoryIdsMatchTheSeededBackendTaxonomy() throws Exception {
        String source = Files.readString(Path.of("..", "frontend", "src", "types", "product.ts"));
        assertTrue(source.contains("'dulces' | 'botanas' | 'paquetes'"),
                "CategoryId must use the backend slugs");

        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value("botanas"))
                .andExpect(jsonPath("$[0].slug").value("botanas"))
                .andExpect(jsonPath("$[0].name").value("Botanas"))
                .andExpect(jsonPath("$[0].shortName").value("Botanas"))
                .andExpect(jsonPath("$[1].id").value("dulces"))
                .andExpect(jsonPath("$[2].id").value("paquetes"));

        for (String id : new String[] {"botanas", "dulces", "paquetes"}) {
            assertTrue(source.contains("'" + id + "'"), "Frontend CategoryId is missing " + id);
        }
    }

    @Test
    void publicProductJsonContainsEveryFieldReadByTheFrontendAdapter() throws Exception {
        insertProduct("contrato", "Producto Contrato");
        String source = Files.readString(Path.of("..", "frontend", "src", "lib", "api", "catalog.ts"));
        assertTrue(source.contains("image: response.imageUrl ?? ''"),
                "Adapter must map nullable imageUrl to the required image field");

        mockMvc.perform(get("/api/catalog/products/contrato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.shortName").exists())
                .andExpect(jsonPath("$.description").exists())
                .andExpect(jsonPath("$.price").exists())
                .andExpect(jsonPath("$.currency").value("MXN"))
                .andExpect(jsonPath("$.category").exists())
                .andExpect(jsonPath("$.kind").exists())
                .andExpect(jsonPath("$.available").exists())
                .andExpect(jsonPath("$.stockStatus").exists())
                .andExpect(jsonPath("$.tags").exists());

        UUID productId = jdbcTemplate.queryForObject(
                "select id from products where slug = 'contrato'", UUID.class);
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into product_images(id,product_id,file_name,content_type,content,created_at,updated_at)
                values (?,?,?,?,?,?,?)
                """, UUID.randomUUID(), productId, "contrato.png", "image/png",
                new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n'},
                Timestamp.from(now), Timestamp.from(now));

        mockMvc.perform(get("/api/catalog/products/contrato"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/api/catalog/products/contrato/image"));
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
                new BigDecimal("10.00"), "MXN", categoryId, null, "[]",
                Timestamp.from(now), Timestamp.from(now));
        jdbcTemplate.update("""
                insert into product_inventory(product_id,available,minimum_stock,version,updated_at)
                values (?,?,?,?,?)
                """, productId, 8, 3, 0L, Timestamp.from(now));
    }
}
