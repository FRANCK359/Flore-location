package com.location.evenement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.location.evenement.dto.request.ProductRequest;
import com.location.evenement.dto.response.ProductResponse;
import com.location.evenement.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("emailtest")
@SpringBootTest(
        properties = {
                "JWT_SECRET=test-secret-for-it",
                "app.jwt.secret=test-secret-for-it"
        }
)
@AutoConfigureMockMvc
class ProductControllerSecurityIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

    private static ProductRequest validRequest() {
        ProductRequest req = new ProductRequest();
        req.setName("Chaise pliante");
        req.setReference("CHAIR-001");
        req.setDescription("Chaise solide");
        req.setPricePerDay(new BigDecimal("12.50"));
        req.setStockQuantity(10);
        req.setIsAvailable(true);
        req.setCategoryId(3L);
        req.setImageUrls(List.of("https://example.com/1.png"));
        req.setMainImage("https://example.com/1.png");
        return req;
    }

    private static ProductResponse sampleResponse(Long id) {
        return new ProductResponse(
                id,
                "Chaise pliante",
                "CHAIR-001",
                "Chaise solide",
                new BigDecimal("12.50"),
                10,
                true,
                3L,
                "Chaises",
                List.of("https://example.com/1.png"),
                "https://example.com/1.png",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    @Test
    void getAllProducts_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void getAllProducts_withAuth_returns200_andJsonArray() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[]"));

        verify(productService).getAllProducts();
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void getProductById_withAuth_returns200() throws Exception {
        when(productService.getProductById(7L)).thenReturn(sampleResponse(7L));

        mockMvc.perform(get("/api/products/7"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.reference").value("CHAIR-001"));

        verify(productService).getProductById(7L);
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void checkStock_withAuth_returns200_andBoolean() throws Exception {
        when(productService.checkStockAvailability(5L, 2)).thenReturn(true);

        mockMvc.perform(get("/api/products/5/check-stock").param("quantity", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        verify(productService).checkStockAvailability(5L, 2);
        verifyNoMoreInteractions(productService);
    }

    @Test
    void createProduct_withoutAuth_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(validRequest());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void createProduct_withAuth_andValidBody_returns201() throws Exception {
        ProductResponse resp = sampleResponse(100L);
        when(productService.createProduct(any(ProductRequest.class))).thenReturn(resp);

        String body = objectMapper.writeValueAsString(validRequest());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.reference").value("CHAIR-001"));

        verify(productService).createProduct(any(ProductRequest.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void createProduct_withAuth_andInvalidBody_returns400() throws Exception {
        ProductRequest invalid = new ProductRequest();
        String body = objectMapper.writeValueAsString(invalid);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void updateProduct_withoutAuth_returns401() throws Exception {
        String body = objectMapper.writeValueAsString(validRequest());

        mockMvc.perform(put("/api/products/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void updateProduct_withAuth_andValidBody_returns200() throws Exception {
        when(productService.updateProduct(eq(9L), any(ProductRequest.class))).thenReturn(sampleResponse(9L));
        String body = objectMapper.writeValueAsString(validRequest());

        mockMvc.perform(put("/api/products/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(9));

        verify(productService).updateProduct(eq(9L), any(ProductRequest.class));
        verifyNoMoreInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void updateProduct_withAuth_andInvalidBody_returns400() throws Exception {
        ProductRequest invalid = validRequest();
        invalid.setName("");
        String body = objectMapper.writeValueAsString(invalid);

        mockMvc.perform(put("/api/products/9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void deleteProduct_withoutAuth_returns401() throws Exception {
        mockMvc.perform(delete("/api/products/4"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productService);
    }

    @Test
    @WithMockUser(username = "user")
    void deleteProduct_withAuth_returns204() throws Exception {
        doNothing().when(productService).deleteProduct(4L);

        mockMvc.perform(delete("/api/products/4"))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(4L);
        verifyNoMoreInteractions(productService);
    }
}