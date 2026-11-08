package com.practice.java_refresher.batch;

import com.practice.java_refresher.batch.config.BatchJobConfig;
import com.practice.java_refresher.batch.dto.ProductCsvDto;
import com.practice.java_refresher.batch.dto.ProductExportDto;
import com.practice.java_refresher.batch.processor.ProductCsvProcessor;
import com.practice.java_refresher.batch.processor.ProductJsonProcessor;
import com.practice.java_refresher.model.Product;
import org.junit.jupiter.api.Test;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.core.io.FileSystemResource;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductBatchTest {

    private final ProductCsvProcessor csvProcessor = new ProductCsvProcessor();
    private final ProductJsonProcessor jsonProcessor = new ProductJsonProcessor();

    @Test
    void testChunkSizeIs500() {
        assertEquals(500, BatchJobConfig.CHUNK_SIZE, "Chunk size must be configured to 500 items per transaction");
    }

    @Test
    void testProducts10kCsvReadingAndChunkProcessing() throws Exception {
        FlatFileItemReader<ProductCsvDto> reader = new FlatFileItemReaderBuilder<ProductCsvDto>()
                .name("testCsvReader")
                .resource(new FileSystemResource("data/products_10k.csv"))
                .delimited()
                .delimiter(",")
                .names("name", "description", "price", "stockQuantity", "category")
                .linesToSkip(1)
                .targetType(ProductCsvDto.class)
                .build();

        reader.open(new ExecutionContext());

        List<Product> firstChunk = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            ProductCsvDto dto = reader.read();
            assertNotNull(dto, "Expected product at index " + i);
            Product product = csvProcessor.process(dto);
            assertNotNull(product, "Product should not be null at index " + i);
            firstChunk.add(product);
        }

        reader.close();

        assertEquals(500, firstChunk.size(), "First chunk must contain exactly 500 processed items");

        Product firstProduct = firstChunk.get(0);
        assertEquals("Pro Compact Monitor Gen-2", firstProduct.getName());
        assertEquals(new BigDecimal("461.95"), firstProduct.getPrice());
        assertEquals(27, firstProduct.getStockQuantity());
        assertEquals("Audio", firstProduct.getCategory());
        assertNotNull(firstProduct.getCreatedAt());
        assertNotNull(firstProduct.getUpdatedAt());
    }

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

