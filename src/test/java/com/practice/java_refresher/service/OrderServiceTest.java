package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.OrderItemRequestDto;
import com.practice.java_refresher.dto.OrderRequestDto;
import com.practice.java_refresher.dto.OrderResponseDto;
import com.practice.java_refresher.exception.InsufficientStockException;
import com.practice.java_refresher.exception.ResourceNotFoundException;
import com.practice.java_refresher.exception.UnauthorizedAccessException;
import com.practice.java_refresher.model.Order;
import com.practice.java_refresher.model.OrderItem;
import com.practice.java_refresher.model.OrderStatus;
import com.practice.java_refresher.model.Product;
import com.practice.java_refresher.repository.OrderRepository;
import com.practice.java_refresher.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "user-123",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        testProduct = Product.builder()
                .id(1L)
                .name("Smartphone")
                .price(new BigDecimal("799.99"))
                .stockQuantity(10)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateOrder_Success() {
        OrderRequestDto request = OrderRequestDto.builder()
                .items(List.of(OrderItemRequestDto.builder().productId(1L).quantity(2).build()))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(100L);
            return o;
        });

        OrderResponseDto response = orderService.createOrder(request);

        assertNotNull(response);
        assertEquals(8, testProduct.getStockQuantity()); // Stock was decremented from 10 to 8
        assertEquals(new BigDecimal("1599.98"), response.getTotalAmount());
        assertEquals(OrderStatus.PENDING, response.getStatus());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateOrder_InsufficientStock() {
        OrderRequestDto request = OrderRequestDto.builder()
                .items(List.of(OrderItemRequestDto.builder().productId(1L).quantity(50).build()))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(InsufficientStockException.class, () -> orderService.createOrder(request));
        assertEquals(10, testProduct.getStockQuantity()); // Stock remains unchanged
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testCreateOrder_ProductNotFound() {
        OrderRequestDto request = OrderRequestDto.builder()
                .items(List.of(OrderItemRequestDto.builder().productId(999L).quantity(1).build()))
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testGetOrderByOrderNumber_AccessDeniedForDifferentUser() {
        Order otherUserOrder = Order.builder()
                .id(200L)
                .orderNumber("ORD-999")
                .userId("different-user")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findByOrderNumber("ORD-999")).thenReturn(Optional.of(otherUserOrder));

        assertThrows(UnauthorizedAccessException.class, () -> orderService.getOrderByOrderNumber("ORD-999"));
    }

    @Test
    void testUpdateOrderStatus_RestoresStockOnCancel() {
        OrderItem item = OrderItem.builder()
                .product(testProduct)
                .quantity(3)
                .unitPrice(new BigDecimal("799.99"))
                .subtotal(new BigDecimal("2399.97"))
                .build();

        Order order = Order.builder()
                .id(300L)
                .orderNumber("ORD-RESTORE")
                .userId("user-123")
                .status(OrderStatus.PROCESSING)
                .items(new ArrayList<>(List.of(item)))
                .build();
        item.setOrder(order);

        when(orderRepository.findByOrderNumber("ORD-RESTORE")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponseDto updated = orderService.updateOrderStatus("ORD-RESTORE", OrderStatus.CANCELLED);

        assertEquals(OrderStatus.CANCELLED, updated.getStatus());
        assertEquals(13, testProduct.getStockQuantity()); // 10 + 3 restored
        verify(productRepository).save(testProduct);
    }
}

