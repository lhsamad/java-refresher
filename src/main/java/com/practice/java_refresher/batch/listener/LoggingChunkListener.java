package com.practice.java_refresher.batch.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.listener.ChunkListener;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoggingChunkListener implements ChunkListener<Object, Object> {

    private static final Logger log = LoggerFactory.getLogger(LoggingChunkListener.class);
    private final AtomicInteger chunkCounter = new AtomicInteger(0);

    @Override
    public void beforeChunk(Chunk<Object> chunk) {
        int chunkNum = chunkCounter.incrementAndGet();
        int itemCount = chunk != null ? chunk.size() : 0;
        log.info(">>> [TRANSACTION START] Processing chunk #{} (Items: {}, Thread: {})",
                chunkNum, itemCount, Thread.currentThread().getName());
    }

    @Override
    public void afterChunk(Chunk<Object> chunk) {
        int writtenCount = chunk != null ? chunk.size() : 0;
        log.info("<<< [TRANSACTION COMMIT] Chunk #{} successfully committed! (Items Written: {})",
                chunkCounter.get(), writtenCount);
    }

    @Override
    public void onChunkError(Exception exception, Chunk<Object> chunk) {
        log.error("XXX [TRANSACTION ROLLBACK] Chunk #{} failed and rolled back! Cause: {}",
                chunkCounter.get(), exception != null ? exception.getMessage() : "unknown");
    }

    public void resetCounter() {
        chunkCounter.set(0);
    }
}

