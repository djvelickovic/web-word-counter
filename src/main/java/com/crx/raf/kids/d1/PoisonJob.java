package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PoisonJob implements Job {
    @Override
    public ScanType getType() {
        return ScanType.POISON;
    }

    @Override
    public Result<String> getQuery() {
        return null;
    }

    @Override
    public Result<CompletableFuture<Result<Map<String, Integer>>>> initiate(Executor executor) {
        return null;
    }
}
