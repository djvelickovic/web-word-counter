package com.crx.raf.kids.d1.files;

import com.crx.raf.kids.d1.ScannerPool;
import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;

public class FileScannerPool extends ScannerPool {

    public FileScannerPool(JobQueue jobQueue, ResultRetrieverPool resultRetrieverPool, int poolSize) {
        super(jobQueue, resultRetrieverPool, poolSize);
    }

    @Override
    public void assignJob(Job job) {

    }
}
