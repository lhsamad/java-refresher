package com.practice.java_refresher.controller;

import com.practice.java_refresher.batch.util.CsvDataGenerator;
import com.practice.java_refresher.security.KeycloakJwtAuthenticationConverter;
import com.practice.java_refresher.security.KeycloakOidcAuthoritiesMapper;
import com.practice.java_refresher.security.SecurityConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.JobInstance;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BatchController.class)
@Import(SecurityConfig.class)
class BatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobOperator jobOperator;

    @MockitoBean(name = "productPipelineJob")
    private Job productPipelineJob;

    @MockitoBean(name = "importProductsJob")
    private Job importProductsJob;

    @MockitoBean
    private JobRepository jobRepository;

    @MockitoBean
    private CsvDataGenerator csvDataGenerator;

    @MockitoBean
    private KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;

    @MockitoBean
    private KeycloakOidcAuthoritiesMapper keycloakOidcAuthoritiesMapper;

    private static final String TEST_EXPORT_PATH = "data/test_exported_download.json";

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Paths.get("data"));
        Files.writeString(Paths.get(TEST_EXPORT_PATH), "[{\"id\": 1, \"name\": \"Test Product\"}]");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get(TEST_EXPORT_PATH));
    }

    @Test
    void testDownloadExportedJson_Success() throws Exception {
        mockMvc.perform(get("/api/batch/download")
                        .param("filePath", TEST_EXPORT_PATH))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_exported_download.json\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("[{\"id\": 1, \"name\": \"Test Product\"}]"));
    }

    @Test
    void testDownloadExportedJson_FileNotFound() throws Exception {
        mockMvc.perform(get("/api/batch/download")
                        .param("filePath", "data/non_existent_file.json"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDownloadExportedJson_PathTraversalRejected() throws Exception {
        mockMvc.perform(get("/api/batch/download")
                        .param("filePath", "../build.gradle"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDownloadExportedJson_ByJobExecutionId() throws Exception {
        Long jobExecutionId = 42L;
        JobParameters params = new JobParametersBuilder()
                .addString("outputJsonPath", TEST_EXPORT_PATH)
                .toJobParameters();

        JobExecution mockExecution = new JobExecution(jobExecutionId, new JobInstance(1L, "productPipelineJob"), params);
        when(jobRepository.getJobExecution(jobExecutionId)).thenReturn(mockExecution);

        mockMvc.perform(get("/api/batch/download")
                        .param("jobExecutionId", jobExecutionId.toString()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"test_exported_download.json\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
