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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
class ProductControllerPostgresE2EIT {

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

    @Test
    @WithMockUser(username = "user")
    void createProduct_thenGetById_endToEnd_withPostgres() throws Exception {
        Category category = new Category();
        category.setName("cat-" + UUID.randomUUID());
        category.setDescription("cat desc");
        category.setDisplayOrder(1);
        Category savedCategory = categoryRepository.save(category);

        ProductRequest req = new ProductRequest();
        req.setName("Produit IT");
        req.setReference("IT-" + UUID.randomUUID());
        req.setDescription("Desc");
        req.setPricePerDay(new BigDecimal("9.99"));
        req.setStockQuantity(5);
        req.setIsAvailable(true);
        req.setCategoryId(savedCategory.getId());

        String createBody = objectMapper.writeValueAsString(req);

        String createResponseJson = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.reference").value(req.getReference()))
                .andExpect(jsonPath("$.categoryId").value(savedCategory.getId()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode created = objectMapper.readTree(createResponseJson);
        long createdId = created.get("id").asLong();
        assertThat(createdId).isPositive();

        mockMvc.perform(get("/api/products/{id}", createdId))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.name").value("Produit IT"))
                .andExpect(jsonPath("$.reference").value(req.getReference()))
                .andExpect(jsonPath("$.categoryId").value(savedCategory.getId()));
    }

    @Test
    void getAllProducts_withoutAuth_returns401_endToEnd() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }
}