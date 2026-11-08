package com.practice.java_refresher.controller;

import com.practice.java_refresher.batch.config.BatchJobConfig;
import com.practice.java_refresher.batch.util.CsvDataGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/batch")
@Tag(name = "Spring Batch Pipeline", description = "Endpoints for running large-scale chunked CSV import and JSON export jobs")
public class BatchController {

    private final JobOperator jobOperator;
    private final Job productPipelineJob;
    private final Job importProductsJob;
    private final JobRepository jobRepository;
    private final CsvDataGenerator csvDataGenerator;

    public BatchController(JobOperator jobOperator,
                           @Qualifier("productPipelineJob") Job productPipelineJob,
                           @Qualifier("importProductsJob") Job importProductsJob,
                           JobRepository jobRepository,
                           CsvDataGenerator csvDataGenerator) {
        this.jobOperator = jobOperator;
        this.productPipelineJob = productPipelineJob;
        this.importProductsJob = importProductsJob;
        this.jobRepository = jobRepository;
        this.csvDataGenerator = csvDataGenerator;
    }

    @PostMapping("/import")
    @Operation(summary = "Import Products CSV (500 items per chunk)",
            description = "Triggers the batch job to import a CSV file (default: data/products_10k.csv) into the PostgreSQL products table 500 items at a time.")
    public ResponseEntity<Map<String, Object>> runImportJob(
            @RequestParam(defaultValue = "data/products_10k.csv") String inputCsvPath) throws Exception {

        File csvFile = new File(inputCsvPath);
        if (!csvFile.exists()) {
            return ResponseEntity.badRequest().body(Map.of("error", "CSV file not found: " + inputCsvPath));
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputCsvPath", inputCsvPath)
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobOperator.start(importProductsJob, jobParameters);

        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", execution.getId());
        response.put("jobName", execution.getJobInstance().getJobName());
        response.put("status", execution.getStatus().toString());
        response.put("startTime", execution.getStartTime() != null ? execution.getStartTime().toString() : "Running");
        response.put("inputCsvPath", inputCsvPath);
        response.put("chunkSize", BatchJobConfig.CHUNK_SIZE);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/run")
    @Operation(summary = "Run the full Batch Pipeline",
            description = "Triggers the two-step chunked batch job: (1) Stream large CSV into PostgreSQL in chunks of 500, then (2) Stream database records into JSON.")
    public ResponseEntity<Map<String, Object>> runBatchJob(
            @RequestParam(defaultValue = "data/products_10k.csv") String inputCsvPath,
            @RequestParam(defaultValue = "data/exported_products.json") String outputJsonPath) throws Exception {

        File csvFile = new File(inputCsvPath);
        if (!csvFile.exists()) {
            csvDataGenerator.generateLargeCsv(inputCsvPath, 2000);
        }

        JobParameters jobParameters = new JobParametersBuilder()
                .addString("inputCsvPath", inputCsvPath)
                .addString("outputJsonPath", outputJsonPath)
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters();

        JobExecution execution = jobOperator.start(productPipelineJob, jobParameters);

        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", execution.getId());
        response.put("jobName", execution.getJobInstance().getJobName());
        response.put("status", execution.getStatus().toString());
        response.put("startTime", execution.getStartTime() != null ? execution.getStartTime().toString() : "Running");
        response.put("inputCsvPath", inputCsvPath);
        response.put("outputJsonPath", outputJsonPath);
        response.put("chunkSize", BatchJobConfig.CHUNK_SIZE);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{jobExecutionId}")
    @Operation(summary = "Get Batch Job Status & Metrics", description = "Inspect the execution state, chunk commit counts, and records processed for a job instance.")
    public ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable Long jobExecutionId) {
        JobExecution execution = jobRepository.getJobExecution(jobExecutionId);
        if (execution == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("jobExecutionId", execution.getId());
        response.put("jobName", execution.getJobInstance().getJobName());
        response.put("status", execution.getStatus().toString());
        response.put("startTime", execution.getStartTime());
        response.put("endTime", execution.getEndTime());
        response.put("exitStatus", execution.getExitStatus().getExitCode());

        Map<String, Object> steps = new HashMap<>();
        execution.getStepExecutions().forEach(step -> {
            Map<String, Object> stepData = new HashMap<>();
            stepData.put("status", step.getStatus().toString());
            stepData.put("itemsRead", step.getReadCount());
            stepData.put("itemsWritten", step.getWriteCount());
            stepData.put("commits", step.getCommitCount());
            stepData.put("rollbacks", step.getRollbackCount());
            stepData.put("filtered", step.getFilterCount());
            steps.put(step.getStepName(), stepData);
        });
        response.put("stepExecutions", steps);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/generate-sample-csv")
    @Operation(summary = "Generate Large Sample CSV", description = "Generates a realistic large product dataset CSV for testing chunking.")
    public ResponseEntity<Map<String, Object>> generateCsv(
            @RequestParam(defaultValue = "data/products_large.csv") String filePath,
            @RequestParam(defaultValue = "2000") int totalRows) throws Exception {

        File file = csvDataGenerator.generateLargeCsv(filePath, totalRows);

        return ResponseEntity.ok(Map.of(
                "filePath", file.getAbsolutePath(),
                "totalRows", totalRows,
                "fileSizeKb", file.length() / 1024,
                "status", "GENERATED"
        ));
    }
}

