package com.crx.raf.kids.d1.result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResultRetrieverPool {

    private static final Logger logger = LoggerFactory.getLogger(ResultRetrieverPool.class);
    private final Map<String, List<CompletableFuture<Map<String, Integer>>>> queryResultsMap = new ConcurrentHashMap<>();

    public void pushResult(String query, CompletableFuture<Map<String, Integer>> result) {
        logger.info("Result: "+query);
        List<CompletableFuture<Map<String, Integer>>> list = queryResultsMap.putIfAbsent(query, new ArrayList<>()); // TODO: reconsider?
        if (list == null) {
            list = queryResultsMap.get(query);
        }
        list.add(result);
    }

}
