package com.practice.java_refresher.batch.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.practice.java_refresher.batch.dto.ProductExportDto;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamWriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ProductJsonStreamWriter implements ItemStreamWriter<ProductExportDto> {

    private final String outputPath;
    private final ObjectMapper objectMapper;
    private BufferedWriter writer;
    private boolean firstItem = true;

    public ProductJsonStreamWriter(String outputPath) {
        this.outputPath = outputPath;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        try {
            File file = new File(outputPath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            this.writer = new BufferedWriter(new FileWriter(file));
            this.writer.write("[\n");
            this.firstItem = true;
        } catch (IOException e) {
            throw new ItemStreamException("Failed to open JSON output stream at: " + outputPath, e);
        }
    }

    @Override
    public void write(Chunk<? extends ProductExportDto> chunk) throws Exception {
        for (ProductExportDto item : chunk) {
            if (!firstItem) {
                writer.write(",\n");
            }
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(item);
            writer.write("  " + json.replace("\n", "\n  "));
            firstItem = false;
        }
        writer.flush();
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
    }

    @Override
    public void close() throws ItemStreamException {
        if (writer != null) {
            try {
                writer.write("\n]\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new ItemStreamException("Failed to close JSON output stream", e);
            }
        }
    }
}

