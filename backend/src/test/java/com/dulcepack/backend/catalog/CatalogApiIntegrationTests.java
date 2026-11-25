package com.dulcepack.backend.catalog;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CatalogApiIntegrationTests {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearProducts() {
        jdbcTemplate.update("delete from product_images");
        jdbcTemplate.update("delete from product_inventory");
        jdbcTemplate.update("delete from products");
    }

    @Test
    void categoriesArePublicAndMatchTheProductFormTaxonomy() throws Exception {
        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("botanas"))
                .andExpect(jsonPath("$[0].name").value("Botanas"))
                .andExpect(jsonPath("$[1].slug").value("dulces"))
                .andExpect(jsonPath("$[1].name").value("Dulces"))
                .andExpect(jsonPath("$[2].slug").value("paquetes"))
                .andExpect(jsonPath("$[2].name").value("Paquetes"));
    }

    @Test
    void catalogFiltersProductsUsingTheFieldsRenderedByTheFrontend() throws Exception {
        insertProduct("box-picosita", "Box Picosita", "Chamoy, tamarindo y chile.",
                "paquetes", "PICANTE", new BigDecimal("159.00"), 7, 10);
        insertProduct("noche-pelis", "Noche de Pelis", "Palomitas, chocolate y más.",
                "botanas", "DULCE", new BigDecimal("219.00"), 15, 5);
        insertProduct("mix-gomitas", "Mix Gomitas", "Suaves, ácidas y frutales.",
                "dulces", "ACIDO", new BigDecimal("129.00"), 38, 12);

        mockMvc.perform(get("/api/catalog/products")
                        .param("query", "PICOSITA")
                        .param("category", "paquetes")
                        .param("flavor", "picante")
                        .param("minPrice", "150")
                        .param("maxPrice", "200"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("box-picosita"))
                .andExpect(jsonPath("$[0].name").value("Box Picosita"))
                .andExpect(jsonPath("$[0].shortName").value("Box Picosita"))
                .andExpect(jsonPath("$[0].price").value(159.0))
                .andExpect(jsonPath("$[0].currency").value("MXN"))
                .andExpect(jsonPath("$[0].category").value("paquetes"))
                .andExpect(jsonPath("$[0].kind").value("paquetes"))
                .andExpect(jsonPath("$[0].flavor").value("picante"))
                .andExpect(jsonPath("$[0].stockStatus").value("LOW"));
    }

    @Test
    void productDetailReturnsAvailabilityAndUnknownSlugIsNotFound() throws Exception {
        insertProduct("fiesta", "Pack Fiesta Dulce", "Dulces variados para compartir",
                "paquetes", "DULCE", new BigDecimal("189.00"), 24, 10);

        mockMvc.perform(get("/api/catalog/products/fiesta"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("fiesta"))
                .andExpect(jsonPath("$.description").value("Dulces variados para compartir"))
                .andExpect(jsonPath("$.available").value(24))
                .andExpect(jsonPath("$.stockStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.imageUrl").doesNotExist());

        mockMvc.perform(get("/api/catalog/products/no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    private void insertProduct(String slug, String name, String description, String categorySlug,
            String flavor, BigDecimal price, int available, int minimum) {
        UUID productId = UUID.randomUUID();
        UUID categoryId = jdbcTemplate.queryForObject(
                "select id from product_categories where slug = ?", UUID.class, categorySlug);
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into products(id,slug,name,short_name,description,search_text,price,currency,category_id,flavor,tags,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?)
                """, productId, slug, name, name, description,
                (name + " " + description).toLowerCase(), price, "MXN", categoryId, flavor,
                "[]", Timestamp.from(now), Timestamp.from(now));
        jdbcTemplate.update("""
                insert into product_inventory(product_id,available,minimum_stock,version,updated_at)
                values (?,?,?,?,?)
                """, productId, available, minimum, 0L, Timestamp.from(now));
    }
}
