package com.practice.java_refresher.batch.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Random;

@Component
public class CsvDataGenerator {

    private static final Logger log = LoggerFactory.getLogger(CsvDataGenerator.class);

    private static final List<String> CATEGORIES = List.of(
            "Laptops", "Audio", "Peripherals", "Mobile", "Monitors", "Accessories", "Office Furniture", "Storage"
    );

    private static final List<String> ADJECTIVES = List.of(
            "Pro", "Ultra", "Max", "Air", "Studio", "Wireless", "Ergonomic", "RGB", "Compact", "Enterprise",
            "Titanium", "Mechanical", "Curved", "Gaming", "Tactile", "Portable", "Fast-Charge", "Smart"
    );

    private static final List<String> BASE_PRODUCTS = List.of(
            "Keyboard", "Mouse", "Headphones", "Monitor", "Laptop", "Standing Desk", "Chair",
            "Power Bank", "Earbuds", "Docking Station", "Webcam", "Microphone", "Tablet", "Cable"
    );

    public File generateLargeCsv(String targetFilePath, int totalRows) throws IOException {
        File file = new File(targetFilePath);
        if (file.getParentFile() != null) {
            file.getParentFile().mkdirs();
        }

        log.info("Generating realistic large CSV dataset with {} rows at '{}'...", totalRows, targetFilePath);
        Random random = new Random(42); // Deterministic seed for reproducible tests

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            // Write CSV header
            writer.write("name,description,price,stockQuantity,category\n");

            for (int i = 1; i <= totalRows; i++) {
                String category = CATEGORIES.get(random.nextInt(CATEGORIES.size()));
                String adj1 = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
                String adj2 = ADJECTIVES.get(random.nextInt(ADJECTIVES.size()));
                String base = BASE_PRODUCTS.get(random.nextInt(BASE_PRODUCTS.size()));

                String productName = String.format("%s %s %s Gen-%d", adj1, adj2, base, (i % 9) + 1);
                String description = String.format("High performance %s engineered for modern tech setups (SKU-%05d).", base.toLowerCase(), i);

                double rawPrice = 19.99 + (random.nextDouble() * 1980.0);
                BigDecimal price = BigDecimal.valueOf(rawPrice).setScale(2, RoundingMode.HALF_UP);
                int stock = random.nextInt(150) + 1;

                // Escape commas or quotes in CSV format if needed
                writer.write(String.format("\"%s\",\"%s\",%.2f,%d,\"%s\"\n",
                        productName, description, price, stock, category));
            }
        }

        log.info("Successfully generated {} rows (file size: {} KB) at '{}'",
                totalRows, file.length() / 1024, targetFilePath);
        return file;
    }
}

