package com.crx.raf.kids.d1;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Pool {

    protected final ExecutorService executorService ;

    public Pool(int poolSize) {
        this.executorService = Executors.newFixedThreadPool(poolSize);
    }
}
