package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.ScanType;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PoisonPillJob implements Job {
    @Override
    public ScanType getType() {
        return ScanType.POISON;
    }

    @Override
    public String getQuery() {
        return null;
    }

    @Override
    public CompletableFuture<Map<String, Integer>> initiate(Executor executor) {
        return null;
    }
}
