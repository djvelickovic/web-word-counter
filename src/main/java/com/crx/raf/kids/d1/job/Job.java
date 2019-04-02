package com.crx.raf.kids.d1.job;

import com.crx.raf.kids.d1.util.Result;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

public interface Job {

    ScanType getType();

    Result<String> getQuery();

    CompletableFuture<Result<Map<String, Integer>>> initiate(Executor executor);

}
