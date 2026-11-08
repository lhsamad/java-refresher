package com.practice.java_refresher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.java_refresher.dto.ProductRequestDto;
import com.practice.java_refresher.dto.ProductResponseDto;
import com.practice.java_refresher.security.KeycloakJwtAuthenticationConverter;
import com.practice.java_refresher.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@Import(com.practice.java_refresher.security.SecurityConfig.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @MockitoBean
    private com.practice.java_refresher.security.KeycloakOidcAuthoritiesMapper keycloakOidcAuthoritiesMapper;

    @Test
    void testGetProducts_PublicAccess() throws Exception {
        ProductResponseDto dto = ProductResponseDto.builder()
                .id(1L)
                .name("Tablet")
                .price(new BigDecimal("499.99"))
                .stockQuantity(15)
                .category("Electronics")
                .build();

        when(productService.getAllProducts(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Tablet"))
                .andExpect(jsonPath("$.content[0].price").value(499.99));
    }

    @Test
    void testCreateProduct_AdminAuthorized() throws Exception {
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name("Smart TV")
                .description("4K OLED")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(20)
                .category("Electronics")
                .build();

        ProductResponseDto responseDto = ProductResponseDto.builder()
                .id(10L)
                .name("Smart TV")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(20)
                .build();

        when(productService.createProduct(any(ProductRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.name").value("Smart TV"));
    }

    @Test
    void testCreateProduct_UserForbidden() throws Exception {
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name("Smart TV")
                .price(new BigDecimal("1299.99"))
                .stockQuantity(20)
                .build();

        mockMvc.perform(post("/api/products")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isForbidden());
    }
}

