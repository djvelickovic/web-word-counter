package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ScannerPool extends Pool {
    protected final JobQueue jobQueue;
    protected final ResultRetrieverPool resultRetrieverPool;

    public ScannerPool(JobQueue jobQueue, ResultRetrieverPool resultRetrieverPool, int poolSize) {
        super(poolSize);
        this.jobQueue = jobQueue;
        this.resultRetrieverPool = resultRetrieverPool;
    }

    public abstract void assignJob(Job job);

}
