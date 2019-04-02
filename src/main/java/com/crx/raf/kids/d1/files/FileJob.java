package com.crx.raf.kids.d1.files;

import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class FileJob implements Job {

    public FileJob() {
    }

    @Override
    public ScanType getType() {
        return null;
    }

    @Override
    public Result<String> getQuery() {
        return null;
    }

    @Override
    public CompletableFuture<Result<Map<String, Integer>>> initiate(Executor executor) {
        return null;
    }
}
