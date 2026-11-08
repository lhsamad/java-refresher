package com.practice.java_refresher.dto;

import com.practice.java_refresher.model.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateDto {

    @NotNull(message = "Order status is required")
    private OrderStatus status;
}

