package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.CartDto;
import com.practice.java_refresher.dto.OrderItemRequestDto;
import com.practice.java_refresher.dto.OrderRequestDto;
import com.practice.java_refresher.dto.OrderResponseDto;
import com.practice.java_refresher.exception.ResourceNotFoundException;
import com.practice.java_refresher.model.Product;
import com.practice.java_refresher.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CartService {

    public static final String CART_SESSION_KEY = "shopping_cart";

    private final ProductRepository productRepository;
    private final OrderService orderService;

    public CartService(ProductRepository productRepository, OrderService orderService) {
        this.productRepository = productRepository;
        this.orderService = orderService;
    }

    public CartDto getCart(HttpSession session) {
        CartDto cart = (CartDto) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new CartDto();
            session.setAttribute(CART_SESSION_KEY, cart);
        }
        return cart;
    }

    public CartDto addToCart(HttpSession session, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        CartDto cart = getCart(session);
        cart.addItem(product, quantity);
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    public CartDto updateQuantity(HttpSession session, Long productId, int quantity) {
        CartDto cart = getCart(session);
        cart.updateQuantity(productId, quantity);
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    public CartDto removeFromCart(HttpSession session, Long productId) {
        CartDto cart = getCart(session);
        cart.removeItem(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    @Transactional
    public OrderResponseDto checkout(HttpSession session) {
        CartDto cart = getCart(session);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Your cart is empty. Add items before checking out.");
        }

        List<OrderItemRequestDto> orderItems = cart.getItemList().stream()
                .map(item -> OrderItemRequestDto.builder()
                        .productId(item.getProductId())
                        .quantity(item.getQuantity())
                        .build())
                .collect(Collectors.toList());

        OrderRequestDto orderRequest = OrderRequestDto.builder()
                .items(orderItems)
                .build();

        OrderResponseDto placedOrder = orderService.createOrder(orderRequest);
        clearCart(session);
        return placedOrder;
    }
}

