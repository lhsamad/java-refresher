package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.ProductRequestDto;
import com.practice.java_refresher.dto.ProductResponseDto;
import com.practice.java_refresher.exception.ResourceNotFoundException;
import com.practice.java_refresher.model.Product;
import com.practice.java_refresher.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = Product.builder()
                .id(1L)
                .name("Mechanical Keyboard")
                .description("RGB backlit mechanical keyboard")
                .price(new BigDecimal("129.99"))
                .stockQuantity(25)
                .category("Electronics")
                .build();
    }

    @Test
    void testGetAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<ProductResponseDto> result = productService.getAllProducts(null, null, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Mechanical Keyboard", result.getContent().get(0).getName());
        verify(productRepository).findAll(pageable);
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

        ProductResponseDto result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Mechanical Keyboard", result.getName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(999L));
    }

    @Test
    void testCreateProduct() {
        ProductRequestDto requestDto = ProductRequestDto.builder()
                .name("Wireless Mouse")
                .description("Ergonomic mouse")
                .price(new BigDecimal("49.99"))
                .stockQuantity(50)
                .category("Electronics")
                .build();

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(2L);
            return p;
        });

        ProductResponseDto result = productService.createProduct(requestDto);

        assertNotNull(result);
        assertEquals("Wireless Mouse", result.getName());
        assertEquals(2L, result.getId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void testUpdateProduct_Success() {
        ProductRequestDto updateDto = ProductRequestDto.builder()
                .name("Mechanical Keyboard V2")
                .description("Updated switches")
                .price(new BigDecimal("139.99"))
                .stockQuantity(30)
                .category("Electronics")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        ProductResponseDto result = productService.updateProduct(1L, updateDto);

        assertNotNull(result);
        assertEquals("Mechanical Keyboard V2", result.getName());
        assertEquals(30, result.getStockQuantity());
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
        verify(productRepository).deleteById(1L);
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(999L));
        verify(productRepository, never()).deleteById(anyLong());
    }
}

