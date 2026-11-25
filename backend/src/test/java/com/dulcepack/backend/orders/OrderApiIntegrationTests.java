package com.dulcepack.backend.orders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTests {

    private static final String CUSTOMER = "cliente@dulcepack.mx";
    private static final String OTHER_CUSTOMER = "otro@dulcepack.mx";
    private static final String ADMIN = "admin@dulcepack.mx";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearOrders() {
        orderRepository.deleteAll();
        jdbcTemplate.execute("ALTER TABLE orders ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    @WithMockUser(username = CUSTOMER, roles = "CUSTOMER")
    void resolvesCatalogAndTotalsOnTheServerInsteadOfTrustingClientValues() throws Exception {
        mockMvc.perform(post("/api/orders")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "customerName": "Isaac Ortiz",
                      "email": "isaac@email.com",
                      "deliveryAddress": "Av. Universidad 120, Col. Centro",
                      "shipping": 0.01,
                      "discount": 999999.99,
                      "couponCode": "DULCE50",
                      "items": [
                        {
                          "productId": "fiesta-dulce",
                          "productName": "Nombre manipulado",
                          "quantity": 2,
                          "unitPrice": 0.01
                        },
                        {
                          "productId": "box-picosita",
                          "productName": "Otro nombre manipulado",
                          "quantity": 1,
                          "unitPrice": 0.01
                        }
                      ]
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/api/orders/DP-\\d{6,}")))
            .andExpect(jsonPath("$.code").value(org.hamcrest.Matchers.matchesPattern("DP-\\d{6,}")))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.subtotal").value(537.00))
            .andExpect(jsonPath("$.shipping").value(79.00))
            .andExpect(jsonPath("$.discount").value(50.00))
            .andExpect(jsonPath("$.total").value(566.00))
            .andExpect(jsonPath("$.items[0].productName").value("Pack Fiesta Dulce"))
            .andExpect(jsonPath("$.items[0].unitPrice").value(189.00))
            .andExpect(jsonPath("$.items[1].productName").value("Box Picosita"))
            .andExpect(jsonPath("$.items[1].unitPrice").value(159.00));
    }

    @Test
    void customerCannotUseAdministrativeOrderOrReportRoutes() throws Exception {
        String code = createOrderAs(CUSTOMER);

        mockMvc.perform(get("/api/admin/orders").with(user(CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isForbidden());
        mockMvc.perform(patch("/api/admin/orders/{code}/status", code)
                .with(user(CUSTOMER).roles("CUSTOMER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PREPARING\"}"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/reports/orders/summary").with(user(CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void orderDetailIsVisibleOnlyToItsOwnerOrAnAdministrator() throws Exception {
        String code = createOrderAs(CUSTOMER);

        mockMvc.perform(get("/api/orders/{code}", code).with(user(CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(code));
        mockMvc.perform(get("/api/orders/{code}", code).with(user(OTHER_CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/{code}", "DP-999999").with(user(OTHER_CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/orders/{code}", code).with(user(ADMIN).roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(code));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void listsOrdersForAdministrationAndReturnsTheirDetail() throws Exception {
        String code = createOrderAs(CUSTOMER);

        mockMvc.perform(get("/api/admin/orders"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].code").value(code))
            .andExpect(jsonPath("$.content[0].customerName").value("Isaac Ortiz"));

        mockMvc.perform(get("/api/orders/{code}", code))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.items[0].productName").value("Pack Fiesta Dulce"));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void advancesTheOrderStatusThroughTheAdministrativeApi() throws Exception {
        String code = createOrderAs(CUSTOMER);

        mockMvc.perform(patch("/api/admin/orders/{code}/status", code)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PREPARING\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.status").value("PREPARING"));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void summarizesOrdersForTheAdministrativeReport() throws Exception {
        createOrderAs(CUSTOMER);

        mockMvc.perform(get("/api/admin/reports/orders/summary"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalOrders").value(1))
            .andExpect(jsonPath("$.grossRevenue").value(268.00))
            .andExpect(jsonPath("$.averageTicket").value(268.00))
            .andExpect(jsonPath("$.ordersByStatus.PENDING").value(1));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void rejectsInvalidOrderStatusTransitions() throws Exception {
        String code = createOrderAs(CUSTOMER);

        mockMvc.perform(patch("/api/admin/orders/{code}/status", code)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DELIVERED\"}"))
            .andExpect(status().isConflict());
    }

    @Test
    void acceptsOrderCodesWithMoreThanSixDigits() throws Exception {
        jdbcTemplate.execute("ALTER TABLE orders ALTER COLUMN id RESTART WITH 1000000");
        String code = createOrderAs(CUSTOMER);

        mockMvc.perform(get("/api/orders/{code}", code).with(user(CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("DP-1000000"));
    }

    @Test
    void rejectsOrderCodesThatOverflowLongWithoutReturningServerError() throws Exception {
        mockMvc.perform(get("/api/orders/{code}", "DP-999999999999999999999999999999999999")
                .with(user(CUSTOMER).roles("CUSTOMER")))
            .andExpect(status().isNotFound());
    }

    @Test
    void rejectsRequestValuesThatExceedDatabaseColumnLimits() throws Exception {
        String longName = "x".repeat(141);
        String longAddress = "x".repeat(301);
        String longProductId = "x".repeat(101);

        createExpectingBadRequest(CUSTOMER, requestBody(longName, "isaac@email.com", "Calle 1", "fiesta-dulce", 1, null));
        createExpectingBadRequest(CUSTOMER, requestBody("Isaac", "isaac@email.com", longAddress, "fiesta-dulce", 1, null));
        createExpectingBadRequest(CUSTOMER, requestBody("Isaac", "isaac@email.com", "Calle 1", longProductId, 1, null));
        createExpectingBadRequest(CUSTOMER, requestBody("Isaac", "isaac@email.com", "Calle 1", "fiesta-dulce", 1_000_001, null));
    }

    @Test
    void rejectsAuthenticationNamesThatExceedOwnerColumnLimit() throws Exception {
        createExpectingBadRequest(
            "x".repeat(255),
            requestBody("Isaac", "isaac@email.com", "Calle 1", "fiesta-dulce", 1, null)
        );
    }

    @Test
    void rejectsUnknownCouponsInsteadOfAcceptingAnIncoherentDiscount() throws Exception {
        createExpectingBadRequest(
            CUSTOMER,
            requestBody("Isaac", "isaac@email.com", "Calle 1", "fiesta-dulce", 1, "DESCUENTO-INVENTADO")
        );
    }

    private String createOrderAs(String username) throws Exception {
        String response = mockMvc.perform(post("/api/orders")
                .with(user(username).roles("CUSTOMER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody("Isaac Ortiz", "isaac@email.com", "Av. Universidad 120, Col. Centro", "fiesta-dulce", 1, null)))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(response, "$.code");
    }

    private void createExpectingBadRequest(String username, String body) throws Exception {
        mockMvc.perform(post("/api/orders")
                .with(user(username).roles("CUSTOMER"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
            .andExpect(status().isBadRequest());
    }

    private static String requestBody(
        String customerName,
        String email,
        String deliveryAddress,
        String productId,
        int quantity,
        String couponCode
    ) {
        String coupon = couponCode == null ? "null" : "\"" + couponCode + "\"";
        return """
            {
              "customerName": "%s",
              "email": "%s",
              "deliveryAddress": "%s",
              "couponCode": %s,
              "items": [{
                "productId": "%s",
                "quantity": %d
              }]
            }
            """.formatted(customerName, email, deliveryAddress, coupon, productId, quantity);
    }
}
