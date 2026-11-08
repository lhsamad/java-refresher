package com.practice.java_refresher.controller;

import com.practice.java_refresher.dto.ProductResponseDto;
import com.practice.java_refresher.security.KeycloakJwtAuthenticationConverter;
import com.practice.java_refresher.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WebViewController.class)
@Import(com.practice.java_refresher.security.SecurityConfig.class)
class WebViewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @MockitoBean
    private com.practice.java_refresher.security.KeycloakOidcAuthoritiesMapper keycloakOidcAuthoritiesMapper;

    @MockitoBean
    private org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository;

    @MockitoBean
    private com.practice.java_refresher.service.AuthService authService;

    @MockitoBean
    private com.practice.java_refresher.service.CartService cartService;

    @MockitoBean
    private com.practice.java_refresher.service.OrderService orderService;

    @MockitoBean
    private org.springframework.batch.core.launch.JobOperator jobOperator;

    @MockitoBean
    private org.springframework.batch.core.job.Job productPipelineJob;

    @MockitoBean
    private com.practice.java_refresher.batch.util.CsvDataGenerator csvDataGenerator;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        when(cartService.getCart(any())).thenReturn(new com.practice.java_refresher.dto.CartDto());
    }

    @Test
    void testHomeView() throws Exception {
        ProductResponseDto product = ProductResponseDto.builder()
                .id(1L)
                .name("Desk Chair")
                .price(new BigDecimal("149.99"))
                .stockQuantity(10)
                .build();

        when(productService.getAllProducts(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(product)));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("totalProducts"));
    }

    @Test
    void testListProductsView() throws Exception {
        when(productService.getAllProducts(any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/web/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("productPage"));
    }

    @Test
    void testProductDetailsView() throws Exception {
        ProductResponseDto product = ProductResponseDto.builder()
                .id(1L)
                .name("Desk Chair")
                .price(new BigDecimal("149.99"))
                .stockQuantity(10)
                .build();

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/web/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/details"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void testLoginView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}

