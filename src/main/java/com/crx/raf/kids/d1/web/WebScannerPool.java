package com.crx.raf.kids.d1.web;

import com.crx.raf.kids.d1.ScannerPool;
import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;
import com.crx.raf.kids.d1.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class WebScannerPool extends ScannerPool {

    private static final Logger logger = LoggerFactory.getLogger(WebScannerPool.class);


    public WebScannerPool(JobQueue jobQueue, ResultRetrieverPool resultRetrieverPool, int poolSize) {
        super(jobQueue, resultRetrieverPool, poolSize);
    }

    @Override
    public void assignJob(Job job) {
        Result<String> queryResult = job.getQuery();
        if (queryResult.isError()) {
            logger.warn("Unable to reach job query. Error: {}", queryResult.getError());
            return;
        }
        Result<CompletableFuture<Result<Map<String, Integer>>>> future = job.initiate(executorService);
        if (future.isError()) {
            // log
            return;
        }
        resultRetrieverPool.addCorpusResult(queryResult.getValue(), future.getValue());;
    }
}
