package com.dulcepack.backend.catalog;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import jakarta.servlet.http.Cookie;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminCatalogApiIntegrationTests {

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
    void productAdministrationRequiresAnAdministrator() throws Exception {
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isUnauthorized());

        register("client-catalog@example.com");
        Cookie client = login("client-catalog@example.com");
        mockMvc.perform(get("/api/admin/products").cookie(client))
                .andExpect(status().isForbidden());
    }

    @Test
    void administratorCreatesListsAndServesAProductImage() throws Exception {
        Cookie admin = adminSession("admin-catalog-create@example.com");
        MockMultipartFile product = jsonPart("""
                {
                  "name":"Pack Fiesta Dulce",
                  "description":"Dulces variados para compartir",
                  "price":189.00,
                  "stock":24,
                  "category":"Paquetes",
                  "tags":["Fiesta","Compartir","Más vendido"],
                  "flavor":"dulce"
                }
                """);
        MockMultipartFile image = new MockMultipartFile("image", "pack-fiesta.png", "image/png",
                "imagen de prueba".getBytes());

        mockMvc.perform(multipart("/api/admin/products")
                        .file(product)
                        .file(image)
                        .cookie(admin)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/products/pack-fiesta-dulce"))
                .andExpect(jsonPath("$.id").value("pack-fiesta-dulce"))
                .andExpect(jsonPath("$.category").value("Paquetes"))
                .andExpect(jsonPath("$.stock").value(24))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.tags[2]").value("Más vendido"))
                .andExpect(jsonPath("$.image.fileName").value("pack-fiesta.png"));

        mockMvc.perform(get("/api/admin/products")
                        .param("query", "FIESTA")
                        .cookie(admin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Pack Fiesta Dulce"));

        mockMvc.perform(get("/api/catalog/products/pack-fiesta-dulce/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(content().bytes("imagen de prueba".getBytes()));
    }

    @Test
    void administratorEditsTheFieldsAndStockRenderedByTheForm() throws Exception {
        Cookie admin = adminSession("admin-catalog-edit@example.com");
        createProduct(admin, "Box Picosita", 159, 7, "Paquetes");

        MockMultipartFile update = jsonPart("""
                {
                  "name":"Box Picosita Especial",
                  "description":"Chamoy, tamarindo y chile",
                  "price":169.00,
                  "stock":9,
                  "category":"Dulces",
                  "tags":["Picosito","Compartir"],
                  "flavor":"picante"
                }
                """);

        mockMvc.perform(multipart(HttpMethod.PUT, "/api/admin/products/box-picosita")
                        .file(update)
                        .cookie(admin)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("box-picosita"))
                .andExpect(jsonPath("$.name").value("Box Picosita Especial"))
                .andExpect(jsonPath("$.price").value(169.0))
                .andExpect(jsonPath("$.stock").value(9))
                .andExpect(jsonPath("$.category").value("Dulces"));
    }

    @Test
    void productValidationRejectsInvalidFieldsAndNonImageFiles() throws Exception {
        Cookie admin = adminSession("admin-catalog-validation@example.com");
        MockMultipartFile invalidProduct = jsonPart("""
                {"name":"","description":"","price":0,"stock":-1,"category":"No existe","tags":[]}
                """);
        MockMultipartFile invalidImage = new MockMultipartFile("image", "notas.txt", "text/plain",
                "texto".getBytes());

        mockMvc.perform(multipart("/api/admin/products")
                        .file(invalidProduct)
                        .file(invalidImage)
                        .cookie(admin)
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    private void createProduct(Cookie admin, String name, int price, int stock, String category) throws Exception {
        MockMultipartFile product = jsonPart("""
                {"name":"%s","description":"Producto de prueba","price":%d,"stock":%d,"category":"%s","tags":["Demo"]}
                """.formatted(name, price, stock, category));
        mockMvc.perform(multipart("/api/admin/products").file(product).cookie(admin).with(csrf()))
                .andExpect(status().isCreated());
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
