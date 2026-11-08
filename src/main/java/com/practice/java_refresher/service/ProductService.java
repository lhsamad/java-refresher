package com.practice.java_refresher.service;

import com.practice.java_refresher.dto.ProductRequestDto;
import com.practice.java_refresher.dto.ProductResponseDto;
import com.practice.java_refresher.exception.ResourceNotFoundException;
import com.practice.java_refresher.model.Product;
import com.practice.java_refresher.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> getAllProducts(String category, String search, Pageable pageable) {
        Page<Product> products;

        if (search != null && !search.trim().isEmpty()) {
            products = productRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            products = productRepository.findByCategoryIgnoreCase(category.trim(), pageable);
        } else {
            products = productRepository.findAll(pageable);
        }

        return products.map(ProductResponseDto::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return ProductResponseDto.fromEntity(product);
    }

    public ProductResponseDto createProduct(ProductRequestDto dto) {
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStockQuantity())
                .category(dto.getCategory())
                .build();

        Product saved = productRepository.save(product);
        return ProductResponseDto.fromEntity(saved);
    }

    public ProductResponseDto updateProduct(Long id, ProductRequestDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setStockQuantity(dto.getStockQuantity());
        product.setCategory(dto.getCategory());

        Product updated = productRepository.save(product);
        return ProductResponseDto.fromEntity(updated);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}

