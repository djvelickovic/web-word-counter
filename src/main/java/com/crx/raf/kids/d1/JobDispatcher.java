package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.files.FileScannerPool;
import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.web.WebScannerPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JobDispatcher implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JobDispatcher.class);

    private final WebScannerPool webScannerPool;
    private final FileScannerPool fileScannerPool;
    private final JobQueue jobQueue;
    private volatile boolean run = true;

    public JobDispatcher(WebScannerPool webScannerPool, FileScannerPool fileScannerPool, JobQueue jobQueue) {
        this.webScannerPool = webScannerPool;
        this.fileScannerPool = fileScannerPool;
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        while (run) {

            try {

                Job job = jobQueue.poll();
                switch (job.getType()) {
                    case WEB:
                        webScannerPool.assignJob(job);
                        break;
                    case FILE:
                        fileScannerPool.assignJob(job);
                        break;
                    case POISON:
                        run = false;
                        break;
                    default:
                        logger.error("Unknown job type {}", job.getType());
                }
            }
            catch (Exception e) {
                logger.error("Error: ", e);
            }
        }
        logger.info("JobDispatcher stopped.");
    }
}
