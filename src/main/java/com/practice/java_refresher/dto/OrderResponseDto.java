package com.practice.java_refresher.dto;

import com.practice.java_refresher.model.Order;
import com.practice.java_refresher.model.OrderStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {

    private Long id;
    private String orderNumber;
    private String userId;
    private String username;
    private String userEmail;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
    private Instant updatedAt;
    private List<OrderItemResponseDto> items;

    public static OrderResponseDto fromEntity(Order order) {
        return OrderResponseDto.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .userId(order.getUserId())
                .username(order.getUsername())
                .userEmail(order.getUserEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .items(order.getItems() != null
                        ? order.getItems().stream().map(OrderItemResponseDto::fromEntity).collect(Collectors.toList())
                        : List.of())
                .build();
    }
}

