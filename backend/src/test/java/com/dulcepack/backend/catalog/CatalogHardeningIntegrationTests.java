package com.dulcepack.backend.catalog;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.http.HttpMethod;
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
class CatalogHardeningIntegrationTests {

    private static final byte[] PNG = new byte[] {
            (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1a, '\n',
            0x00, 0x00, 0x00, 0x0d, 'I', 'H', 'D', 'R'
    };

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
    void rejectsTextFileDisguisedAsPng() throws Exception {
        Cookie admin = adminSession("hardening-png@example.com");
        MockMultipartFile product = jsonPart("""
                {"name":"Producto PNG","description":"Demo","price":10,"stock":1,"category":"Dulces","tags":[]}
                """);
        MockMultipartFile fake = new MockMultipartFile("image", "fake.png", "image/png",
                "this is not a png".getBytes());

        mockMvc.perform(multipart("/api/admin/products").file(product).file(fake)
                        .cookie(admin).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_IMAGE"));
    }

    @Test
    void rejectsSvgImageWithScriptContent() throws Exception {
        Cookie admin = adminSession("hardening-svg@example.com");
        MockMultipartFile product = jsonPart("""
                {"name":"Producto SVG","description":"Demo","price":10,"stock":1,"category":"Dulces","tags":[]}
                """);
        MockMultipartFile svg = new MockMultipartFile("image", "malicioso.svg", "image/svg+xml",
                "<svg><script>alert(1)</script></svg>".getBytes());

        mockMvc.perform(multipart("/api/admin/products").file(product).file(svg)
                        .cookie(admin).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PRODUCT_IMAGE"));
    }

    @Test
    void servesImagesInlineWithNoSniffAndSandboxedCsp() throws Exception {
        Cookie admin = adminSession("hardening-headers@example.com");
        createProductWithImage(admin, "Imagen Segura", 3, 0, PNG);

        mockMvc.perform(get("/api/catalog/products/imagen-segura/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Content-Security-Policy", "sandbox"))
                .andExpect(header().string("Content-Disposition",
                        org.hamcrest.Matchers.startsWith("inline; filename=\"seguro.png\"")));
    }

    @Test
    void rejectsMultipartWithoutProductPart() throws Exception {
        Cookie admin = adminSession("hardening-part@example.com");
        MockMultipartFile image = new MockMultipartFile("image", "seguro.png", "image/png", PNG);

        mockMvc.perform(multipart("/api/admin/products").file(image).cookie(admin).with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_REQUEST"));
    }

    @Test
    void allowsConfiguredFrontendOrigin() throws Exception {
        mockMvc.perform(options("/api/catalog/products")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"));
    }

    @Test
    void wildcardSearchDoesNotReturnEveryProduct() throws Exception {
        insertProduct("box-picosita", "Box Picosita", new BigDecimal("159.00"), 7, 10);

        mockMvc.perform(get("/api/catalog/products").param("query", "%"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void minimumStockIsWritableAndDrivesLowStatus() throws Exception {
        Cookie admin = adminSession("hardening-minimum@example.com");
        MockMultipartFile product = jsonPart("""
                {"name":"Stock Bajo","description":"Demo","price":10,"stock":3,"minimumStock":5,
                 "category":"Dulces","tags":[]}
                """);

        mockMvc.perform(multipart("/api/admin/products").file(product).cookie(admin).with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.minimumStock").value(5))
                .andExpect(jsonPath("$.status").value("LOW"));
    }

    @Test
    void secondProductWithSameSlugReturnsSpecificConflict() throws Exception {
        Cookie admin = adminSession("hardening-duplicate@example.com");
        MockMultipartFile product = jsonPart("""
                {"name":"Producto Unico","description":"Demo","price":10,"stock":1,"category":"Dulces","tags":[]}
                """);
        mockMvc.perform(multipart("/api/admin/products").file(product).cookie(admin).with(csrf()))
                .andExpect(status().isCreated());

        mockMvc.perform(multipart("/api/admin/products").file(product).cookie(admin).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PRODUCT_ALREADY_EXISTS"));
    }

    @Test
    void administratorDeletesProduct() throws Exception {
        Cookie admin = adminSession("hardening-delete@example.com");
        createProductWithImage(admin, "Producto Temporal", 2, 0, PNG);

        mockMvc.perform(delete("/api/admin/products/producto-temporal").cookie(admin).with(csrf()))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/catalog/products/producto-temporal"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletionRequiresAdministrator() throws Exception {
        createProductWithImage(adminSession("hardening-delete-auth@example.com"), "Producto Protegido", 2, 0, PNG);
        register("hardening-client@example.com");
        Cookie client = login("hardening-client@example.com");

        mockMvc.perform(delete("/api/admin/products/producto-protegido").cookie(client).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void pagedCatalogReturnsPagedEnvelope() throws Exception {
        insertProduct("producto-uno", "Producto Uno", new BigDecimal("10.00"), 5, 1);
        insertProduct("producto-dos", "Producto Dos", new BigDecimal("20.00"), 5, 1);

        mockMvc.perform(get("/api/catalog/products/page").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void categoriesExposeStableIdsForTheFrontend() throws Exception {
        mockMvc.perform(get("/api/catalog/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("botanas"))
                .andExpect(jsonPath("$[0].slug").value("botanas"))
                .andExpect(jsonPath("$[0].shortName").value("Botanas"));
    }

    private void createProductWithImage(Cookie admin, String name, int stock, int minimum, byte[] image)
            throws Exception {
        MockMultipartFile product = jsonPart("""
                {"name":"%s","description":"Demo","price":10,"stock":%d,"minimumStock":%d,
                 "category":"Dulces","tags":[]}
                """.formatted(name, stock, minimum));
        MockMultipartFile file = new MockMultipartFile("image", "seguro.png", "image/png", image);
        mockMvc.perform(multipart("/api/admin/products").file(product).file(file).cookie(admin).with(csrf()))
                .andExpect(status().isCreated());
    }

    private void insertProduct(String slug, String name, BigDecimal price, int available, int minimum) {
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
                """, productId, available, minimum, 0L, Timestamp.from(now));
    }

    private MockMultipartFile jsonPart(String json) {
        return new MockMultipartFile("product", "product.json", MediaType.APPLICATION_JSON_VALUE,
                json.getBytes());
    }

    private Cookie adminSession(String email) throws Exception {
        createAdmin(email);
        return login(email);
    }

    private void createAdmin(String email) {
        Instant now = Instant.now();
        jdbcTemplate.update("""
                insert into users(id,email,display_name,password_hash,role,status,password_changed_at,created_at,updated_at)
                values (?,?,?,?,?,?,?,?,?)
                """, UUID.randomUUID(), email, "Administrador", passwordEncoder.encode("Str0ng!Password"),
                "ADMIN", "ACTIVE", Timestamp.from(now), Timestamp.from(now), Timestamp.from(now));
    }

    private void register(String email) throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"Str0ng!Password","displayName":"Cliente"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
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
