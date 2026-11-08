package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.CartDto;
import com.practice.java_refresher.dto.OrderResponseDto;
import com.practice.java_refresher.model.OrderStatus;
import com.practice.java_refresher.model.Product;
import com.practice.java_refresher.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private CartService cartService;

    private MockHttpSession session;
    private Product product;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        product = Product.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .category("Peripherals")
                .build();
    }

    @Test
    void testAddToCartAndCalculateTotal() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        CartDto cart = cartService.addToCart(session, 1L, 2);

        assertNotNull(cart);
        assertEquals(2, cart.getTotalQuantity());
        assertEquals(new BigDecimal("200.00"), cart.getTotalAmount());
        assertEquals(1, cart.getItemList().size());
    }

    @Test
    void testUpdateQuantityAndRemove() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addToCart(session, 1L, 2);

        cartService.updateQuantity(session, 1L, 5);
        CartDto cart = cartService.getCart(session);
        assertEquals(5, cart.getTotalQuantity());
        assertEquals(new BigDecimal("500.00"), cart.getTotalAmount());

        cartService.removeFromCart(session, 1L);
        assertTrue(cart.isEmpty());
    }

    @Test
    void testCheckoutSuccess() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        cartService.addToCart(session, 1L, 1);

        OrderResponseDto orderResponse = OrderResponseDto.builder()
                .id(10L)
                .orderNumber("ORD-SUCCESS")
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .build();

        when(orderService.createOrder(any())).thenReturn(orderResponse);

        OrderResponseDto placed = cartService.checkout(session);

        assertNotNull(placed);
        assertEquals("ORD-SUCCESS", placed.getOrderNumber());
        // Verify cart is cleared after checkout
        assertNull(session.getAttribute(CartService.CART_SESSION_KEY));
    }

    @Test
    void testCheckoutEmptyCartThrows() {
        assertThrows(IllegalStateException.class, () -> cartService.checkout(session));
    }
}

