package com.practice.java_refresher.controller;

import com.practice.java_refresher.dto.OrderResponseDto;
import com.practice.java_refresher.dto.OrderStatusUpdateDto;
import com.practice.java_refresher.model.OrderStatus;
import com.practice.java_refresher.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Orders", description = "Admin order management and status update endpoints")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    @Operation(summary = "List all orders (Admin)", description = "Retrieve all customer orders with optional status filter and pagination.")
    public ResponseEntity<Page<OrderResponseDto>> getAllOrders(
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(orderService.getAllOrders(status, pageable));
    }

    @PatchMapping("/{orderNumber}/status")
    @Operation(summary = "Update order status (Admin)", description = "Update the processing or shipment status of an order.")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(
            @PathVariable String orderNumber,
            @Valid @RequestBody OrderStatusUpdateDto statusUpdateDto) {
        return ResponseEntity.ok(orderService.updateOrderStatus(orderNumber, statusUpdateDto.getStatus()));
    }
}

