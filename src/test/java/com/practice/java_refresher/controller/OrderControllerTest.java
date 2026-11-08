package com.practice.java_refresher.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practice.java_refresher.dto.OrderItemRequestDto;
import com.practice.java_refresher.dto.OrderRequestDto;
import com.practice.java_refresher.dto.OrderResponseDto;
import com.practice.java_refresher.model.OrderStatus;
import com.practice.java_refresher.security.KeycloakJwtAuthenticationConverter;
import com.practice.java_refresher.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
@Import(com.practice.java_refresher.security.SecurityConfig.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @MockitoBean
    private com.practice.java_refresher.security.KeycloakOidcAuthoritiesMapper keycloakOidcAuthoritiesMapper;

    @Test
    void testGetOrders_Unauthenticated_ForbiddenOrUnauthorized() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetOrders_AuthenticatedUser_Success() throws Exception {
        OrderResponseDto order = OrderResponseDto.builder()
                .id(1L)
                .orderNumber("ORD-12345")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build();

        when(orderService.getCurrentUserOrders()).thenReturn(List.of(order));

        mockMvc.perform(get("/api/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderNumber").value("ORD-12345"))
                .andExpect(jsonPath("$[0].totalAmount").value(99.99));
    }

    @Test
    void testCreateOrder_Success() throws Exception {
        OrderRequestDto request = OrderRequestDto.builder()
                .items(List.of(OrderItemRequestDto.builder().productId(1L).quantity(2).build()))
                .build();

        OrderResponseDto response = OrderResponseDto.builder()
                .id(2L)
                .orderNumber("ORD-98765")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("199.98"))
                .build();

        when(orderService.createOrder(any(OrderRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/orders")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-98765"));
    }
}

