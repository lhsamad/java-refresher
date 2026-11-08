package com.practice.java_refresher.batch;

import com.practice.java_refresher.batch.dto.ProductCsvDto;
import com.practice.java_refresher.batch.dto.ProductExportDto;
import com.practice.java_refresher.batch.processor.ProductCsvProcessor;
import com.practice.java_refresher.batch.processor.ProductJsonProcessor;
import com.practice.java_refresher.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ProductBatchTest {

    private final ProductCsvProcessor csvProcessor = new ProductCsvProcessor();
    private final ProductJsonProcessor jsonProcessor = new ProductJsonProcessor();

    @Test
    void testCsvProcessor_ValidRow() {
        ProductCsvDto dto = ProductCsvDto.builder()
                .name("Mechanical Keyboard")
                .description("RGB backlit")
                .price("$149.99")
                .stockQuantity("25")
                .category("Peripherals")
                .build();

        Product product = csvProcessor.process(dto);

        assertNotNull(product);
        assertEquals("Mechanical Keyboard", product.getName());
        assertEquals(new BigDecimal("149.99"), product.getPrice());
        assertEquals(25, product.getStockQuantity());
        assertEquals("Peripherals", product.getCategory());
    }

    @Test
    void testCsvProcessor_EmptyNameSkipped() {
        ProductCsvDto dto = ProductCsvDto.builder()
                .name("   ")
                .price("10.00")
                .stockQuantity("5")
                .build();

        Product product = csvProcessor.process(dto);
        assertNull(product, "Empty name row should be skipped (returned null)");
    }

    @Test
    void testJsonProcessor_EnrichesFields() {
        Product product = Product.builder()
                .id(100L)
                .name("4K Monitor")
                .category("Displays")
                .price(new BigDecimal("499.00"))
                .stockQuantity(15)
                .description("IPS HDR")
                .build();

        ProductExportDto exportDto = jsonProcessor.process(product);

        assertNotNull(exportDto);
        assertEquals(100L, exportDto.getId());
        assertEquals("4K Monitor", exportDto.getName());
        assertEquals("YES", exportDto.getInStock());
        assertNotNull(exportDto.getExportedAt());
    }
}

