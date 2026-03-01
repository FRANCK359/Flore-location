package com.location.evenement.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.location.evenement.dto.request.ProductRequest;
import com.location.evenement.model.Category;
import com.location.evenement.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(
        properties = {
                "JWT_SECRET=test-secret-for-it",
                "app.jwt.secret=test-secret-for-it",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never",
                "spring.jpa.show-sql=false"
        }
)
@AutoConfigureMockMvc
class ProductControllerPostgresE2EEndpointsIT {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15-alpine")
                    .withDatabaseName("evenement_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    CategoryRepository categoryRepository;

    private Category createCategory(String name) {
        Category c = new Category();
        c.setName(name);
        c.setDescription("desc");
        c.setDisplayOrder(1);
        return categoryRepository.save(c);
    }

    private long createProductViaApi(long categoryId, String reference) throws Exception {
        ProductRequest req = new ProductRequest();
        req.setName("Produit IT");
        req.setReference(reference);
        req.setDescription("Desc");
        req.setPricePerDay(new BigDecimal("9.99"));
        req.setStockQuantity(5);
        req.setIsAvailable(true);
        req.setCategoryId(categoryId);

        String responseJson = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.reference").value(reference))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(responseJson);
        long id = created.get("id").asLong();
        assertThat(id).isPositive();
        return id;
    }

    @Test
    @WithMockUser(username = "user")
    void getByReference_endToEnd_withPostgres() throws Exception {
        Category cat = createCategory("cat-" + UUID.randomUUID());
        String ref = "REF-" + UUID.randomUUID();
        createProductViaApi(cat.getId(), ref);

        mockMvc.perform(get("/api/products/reference/{reference}", ref))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.reference").value(ref))
                .andExpect(jsonPath("$.categoryId").value(cat.getId()));
    }

    @Test
    @WithMockUser(username = "user")
    void getByCategory_endToEnd_withPostgres() throws Exception {
        Category cat = createCategory("cat-" + UUID.randomUUID());

        String ref1 = "REF-" + UUID.randomUUID();
        String ref2 = "REF-" + UUID.randomUUID();

        createProductViaApi(cat.getId(), ref1);
        createProductViaApi(cat.getId(), ref2);

        mockMvc.perform(get("/api/products/category/{categoryId}", cat.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray());
        // Note: on ne force pas la taille exacte, car un DataInitializer pourrait injecter d'autres données.
    }

    @Test
    @WithMockUser(username = "user")
    void updateProduct_put_endToEnd_withPostgres() throws Exception {
        Category cat = createCategory("cat-" + UUID.randomUUID());
        String ref = "REF-" + UUID.randomUUID();
        long productId = createProductViaApi(cat.getId(), ref);

        ProductRequest updateReq = new ProductRequest();
        updateReq.setName("Produit IT UPDATED");
        updateReq.setReference(ref); // garder la même référence = OK
        updateReq.setDescription("Desc updated");
        updateReq.setPricePerDay(new BigDecimal("19.99"));
        updateReq.setStockQuantity(7);
        updateReq.setIsAvailable(true);
        updateReq.setCategoryId(cat.getId());

        mockMvc.perform(put("/api/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Produit IT UPDATED"))
                .andExpect(jsonPath("$.pricePerDay").value(19.99))
                .andExpect(jsonPath("$.stockQuantity").value(7));
    }

    @Test
    @WithMockUser(username = "user")
    void deleteProduct_thenGetById_shouldReturn404_endToEnd_withPostgres() throws Exception {
        Category cat = createCategory("cat-" + UUID.randomUUID());
        String ref = "REF-" + UUID.randomUUID();
        long productId = createProductViaApi(cat.getId(), ref);

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());
    }
}