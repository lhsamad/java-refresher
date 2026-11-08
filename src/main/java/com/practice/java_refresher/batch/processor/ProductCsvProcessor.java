package com.practice.java_refresher.batch.processor;

import com.practice.java_refresher.batch.dto.ProductCsvDto;
import com.practice.java_refresher.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

@Component
public class ProductCsvProcessor implements ItemProcessor<ProductCsvDto, Product> {

    private static final Logger log = LoggerFactory.getLogger(ProductCsvProcessor.class);

    @Override
    public Product process(ProductCsvDto item) {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            log.warn("Skipping CSV row with empty product name.");
            return null; // Returning null skips the item
        }

        try {
            String cleanPrice = item.getPrice() != null
                    ? item.getPrice().replace("$", "").replace(",", "").trim()
                    : "0.00";
            BigDecimal price = new BigDecimal(cleanPrice);

            int stock = item.getStockQuantity() != null && !item.getStockQuantity().trim().isEmpty()
                    ? Integer.parseInt(item.getStockQuantity().trim())
                    : 0;

            Instant now = Instant.now();
            return Product.builder()
                    .name(item.getName().trim())
                    .description(item.getDescription() != null ? item.getDescription().trim() : "")
                    .price(price)
                    .stockQuantity(stock)
                    .category(item.getCategory() != null ? item.getCategory().trim() : "Uncategorized")
                    .createdAt(now)
                    .updatedAt(now)
                    .build();

        } catch (Exception ex) {
            log.error("Error processing CSV row for product '{}': {}", item.getName(), ex.getMessage());
            return null; // Skip corrupted record
        }
    }
}
