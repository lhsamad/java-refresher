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
import com.practice.java_refresher.security.SecurityUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    public OrderResponseDto createOrder(OrderRequestDto request) {
        String userId = SecurityUtils.getCurrentUserId();
        String username = SecurityUtils.getCurrentUsername();
        String userEmail = SecurityUtils.getCurrentUserEmail();

        String orderNumber = "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .userId(userId)
                .username(username)
                .userEmail(userEmail)
                .status(OrderStatus.PENDING)
                .items(new ArrayList<>())
                .totalAmount(BigDecimal.ZERO)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemRequestDto itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + itemRequest.getProductId()));

            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new InsufficientStockException(String.format(
                        "Insufficient stock for product '%s'. Requested: %d, Available: %d",
                        product.getName(), itemRequest.getQuantity(), product.getStockQuantity()
                ));
            }

            // Decrement inventory
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .productName(product.getName())
                    .unitPrice(product.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .subtotal(subtotal)
                    .build();

            order.addItem(orderItem);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        return OrderResponseDto.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getCurrentUserOrders() {
        String userId = SecurityUtils.getCurrentUserId();
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return orders.stream()
                .map(OrderResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByOrderNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

        boolean isAdmin = SecurityUtils.hasRole("ADMIN");
        String currentUserId = SecurityUtils.getCurrentUserId();

        if (!isAdmin && !order.getUserId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("You are not authorized to view this order");
        }

        return OrderResponseDto.fromEntity(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(OrderStatus status, Pageable pageable) {
        Page<Order> orders;
        if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }
        return orders.map(OrderResponseDto::fromEntity);
    }

    public OrderResponseDto updateOrderStatus(String orderNumber, OrderStatus newStatus) {
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNumber));

        // If order is cancelled, restore stock
        if (newStatus == OrderStatus.CANCELLED && order.getStatus() != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() != null) {
                    Product product = item.getProduct();
                    product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(newStatus);
        Order updated = orderRepository.save(order);
        return OrderResponseDto.fromEntity(updated);
    }
}

