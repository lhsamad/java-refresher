package com.practice.java_refresher.batch.processor;

import com.practice.java_refresher.batch.dto.ProductExportDto;
import com.practice.java_refresher.model.Product;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ProductJsonProcessor implements ItemProcessor<Product, ProductExportDto> {

    @Override
    public ProductExportDto process(Product product) {
        return ProductExportDto.builder()
                .id(product.getId())
                .name(product.getName())
                .category(product.getCategory())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .description(product.getDescription())
                .inStock(product.getStockQuantity() != null && product.getStockQuantity() > 0 ? "YES" : "OUT_OF_STOCK")
                .exportedAt(Instant.now())
                .build();
    }
}
