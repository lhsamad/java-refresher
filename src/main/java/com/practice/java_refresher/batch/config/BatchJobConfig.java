package com.practice.java_refresher.batch.config;

import com.practice.java_refresher.batch.dto.ProductCsvDto;
import com.practice.java_refresher.batch.dto.ProductExportDto;
import com.practice.java_refresher.batch.listener.JobCompletionNotificationListener;
import com.practice.java_refresher.batch.listener.LoggingChunkListener;
import com.practice.java_refresher.batch.processor.ProductCsvProcessor;
import com.practice.java_refresher.batch.processor.ProductJsonProcessor;
import com.practice.java_refresher.batch.writer.ProductJsonStreamWriter;
import com.practice.java_refresher.model.Product;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.database.JdbcBatchItemWriter;
import org.springframework.batch.infrastructure.item.database.JdbcCursorItemReader;
import org.springframework.batch.infrastructure.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.infrastructure.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchJobConfig {

    public static final int CHUNK_SIZE = 50;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DataSource dataSource;
    private final ProductCsvProcessor productCsvProcessor;
    private final ProductJsonProcessor productJsonProcessor;
    private final LoggingChunkListener loggingChunkListener;
    private final JobCompletionNotificationListener jobCompletionListener;

    public BatchJobConfig(JobRepository jobRepository,
                          PlatformTransactionManager transactionManager,
                          DataSource dataSource,
                          ProductCsvProcessor productCsvProcessor,
                          ProductJsonProcessor productJsonProcessor,
                          LoggingChunkListener loggingChunkListener,
                          JobCompletionNotificationListener jobCompletionListener) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.dataSource = dataSource;
        this.productCsvProcessor = productCsvProcessor;
        this.productJsonProcessor = productJsonProcessor;
        this.loggingChunkListener = loggingChunkListener;
        this.jobCompletionListener = jobCompletionListener;
    }

    // =========================================================================
    // STEP 1: IMPORT CSV TO DATABASE (Stream File -> Chunk Processing -> DB)
    // =========================================================================

    @Bean
    @StepScope
    public FlatFileItemReader<ProductCsvDto> csvProductReader(
            @Value("#{jobParameters['inputCsvPath'] ?: 'data/products_large.csv'}") String inputCsvPath) {
        return new FlatFileItemReaderBuilder<ProductCsvDto>()
                .name("csvProductReader")
                .resource(new FileSystemResource(inputCsvPath))
                .delimited()
                .delimiter(",")
                .names("name", "description", "price", "stockQuantity", "category")
                .linesToSkip(1) // skip CSV header
                .targetType(ProductCsvDto.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Product> jdbcProductWriter() {
        return new JdbcBatchItemWriterBuilder<Product>()
                .dataSource(dataSource)
                .sql("""
                     INSERT INTO products (name, description, price, stock_quantity, category, created_at, updated_at)
                     VALUES (:name, :description, :price, :stockQuantity, :category, NOW(), NOW())
                     """)
                .beanMapped()
                .build();
    }

    @Bean
    public Step importCsvToDbStep(FlatFileItemReader<ProductCsvDto> csvProductReader) {
        return new StepBuilder("importCsvToDbStep", jobRepository)
                .<ProductCsvDto, Product>chunk(CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(csvProductReader)
                .processor(productCsvProcessor)
                .writer(jdbcProductWriter())
                .listener(loggingChunkListener)
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(20)
                .build();
    }

    // =========================================================================
    // STEP 2: EXPORT DATABASE TO JSON (Stream DB -> Chunk Processing -> JSON)
    // =========================================================================

    @Bean
    public JdbcCursorItemReader<Product> dbProductReader() {
        return new JdbcCursorItemReaderBuilder<Product>()
                .name("dbProductReader")
                .dataSource(dataSource)
                .sql("SELECT id, name, description, price, stock_quantity, category, created_at, updated_at FROM products ORDER BY id ASC")
                .rowMapper((rs, rowNum) -> Product.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .description(rs.getString("description"))
                        .price(rs.getBigDecimal("price"))
                        .stockQuantity(rs.getInt("stock_quantity"))
                        .category(rs.getString("category"))
                        .createdAt(rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toInstant() : null)
                        .updatedAt(rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toInstant() : null)
                        .build())
                .build();
    }

    @Bean
    @StepScope
    public ProductJsonStreamWriter jsonProductWriter(
            @Value("#{jobParameters['outputJsonPath'] ?: 'data/exported_products.json'}") String outputJsonPath) {
        return new ProductJsonStreamWriter(outputJsonPath);
    }

    @Bean
    public Step exportDbToJsonStep(ProductJsonStreamWriter jsonProductWriter) {
        return new StepBuilder("exportDbToJsonStep", jobRepository)
                .<Product, ProductExportDto>chunk(CHUNK_SIZE)
                .transactionManager(transactionManager)
                .reader(dbProductReader())
                .processor(productJsonProcessor)
                .writer(jsonProductWriter)
                .listener(loggingChunkListener)
                .build();
    }

    // =========================================================================
    // THE OVERALL BATCH PIPELINE JOB
    // =========================================================================

    @Bean
    public Job productPipelineJob(Step importCsvToDbStep, Step exportDbToJsonStep) {
        return new JobBuilder("productPipelineJob", jobRepository)
                .listener(jobCompletionListener)
                .start(importCsvToDbStep)
                .next(exportDbToJsonStep)
                .build();
    }
}

