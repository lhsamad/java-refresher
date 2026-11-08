package com.practice.java_refresher.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListener;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class JobCompletionNotificationListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(JobCompletionNotificationListener.class);
    private final LoggingChunkListener chunkListener;

    public JobCompletionNotificationListener(LoggingChunkListener chunkListener) {
        this.chunkListener = chunkListener;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        chunkListener.resetCounter();
        log.info("================================================================================");
        log.info("🚀 SPRING BATCH JOB STARTED: '{}' [Instance ID: {}]",
                jobExecution.getJobInstance().getJobName(), jobExecution.getId());
        log.info("Parameters: {}", jobExecution.getJobParameters());
        log.info("================================================================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long durationMs = 0;
        if (jobExecution.getStartTime() != null && jobExecution.getEndTime() != null) {
            durationMs = Duration.between(jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();
        }

        log.info("================================================================================");
        if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
            log.info("✅ SPRING BATCH JOB COMPLETED SUCCESSFULLY in {} ms", durationMs);
        } else {
            log.error("❌ SPRING BATCH JOB FAILED with status: {}", jobExecution.getStatus());
        }

        for (StepExecution step : jobExecution.getStepExecutions()) {
            log.info("  Step: '{}' | Status: {} | Read: {} | Written: {} | Commits: {} | Rollbacks: {} | Filtered: {}",
                    step.getStepName(), step.getStatus(), step.getReadCount(),
                    step.getWriteCount(), step.getCommitCount(), step.getRollbackCount(), step.getFilterCount());
        }
        log.info("================================================================================");
    }
}

