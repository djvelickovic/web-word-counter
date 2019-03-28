package com.crx.raf.kids.d1.web;

import com.crx.raf.kids.d1.ScannerPool;
import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WebScannerPool extends ScannerPool {

    private static final Logger logger = LoggerFactory.getLogger(WebScannerPool.class);

    public WebScannerPool(JobQueue jobQueue, ResultRetrieverPool resultRetrieverPool, int poolSize) {
        super(jobQueue, resultRetrieverPool, poolSize);
    }

    @Override
    public void assignJob(Job job) {
        CompletableFuture<Map<String, Integer>> future = job.initiate(executorService);
        String query = job.getQuery();
        logger.info("Assigned {} job with query {}",job.getType(), job.getQuery());
        resultRetrieverPool.addCorpusResult(query, future);
    }
}
