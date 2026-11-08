package com.practice.java_refresher.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductCsvDto {
    private String name;
    private String description;
    private String price;
    private String stockQuantity;
    private String category;
}

