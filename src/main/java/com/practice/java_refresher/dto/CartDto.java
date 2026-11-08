package com.practice.java_refresher.dto;

import com.practice.java_refresher.model.Product;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CartDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Map<Long, CartItemDto> items = new LinkedHashMap<>();

    public List<CartItemDto> getItemList() {
        return new ArrayList<>(items.values());
    }

    public void addItem(Product product, int quantity) {
        if (product == null || quantity <= 0) {
            return;
        }

        CartItemDto existing = items.get(product.getId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            items.put(product.getId(), CartItemDto.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .category(product.getCategory())
                    .unitPrice(product.getPrice())
                    .quantity(quantity)
                    .build());
        }
    }

    public void updateQuantity(Long productId, int quantity) {
        if (quantity <= 0) {
            items.remove(productId);
        } else if (items.containsKey(productId)) {
            items.get(productId).setQuantity(quantity);
        }
    }

    public void removeItem(Long productId) {
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotalAmount() {
        return items.values().stream()
                .map(CartItemDto::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalQuantity() {
        return items.values().stream()
                .mapToInt(CartItemDto::getQuantity)
                .sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}

