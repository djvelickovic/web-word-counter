package com.crx.raf.kids.d1.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class JobQueue {

    private static final Logger logger = LoggerFactory.getLogger(JobQueue.class);

    private BlockingQueue<Job> queue = new LinkedBlockingQueue<>();

    public void add(Job job) {
        logger.info("Added {} job to queue. Query: {}", job.getType(), job.getQuery());
        queue.add(job);
    }

    public Job poll() {
        try {
            return queue.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }
}
