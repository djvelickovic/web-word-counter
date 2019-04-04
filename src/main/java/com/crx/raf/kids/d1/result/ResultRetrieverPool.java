package com.crx.raf.kids.d1.result;

import com.crx.raf.kids.d1.Pool;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Error;
import com.crx.raf.kids.d1.util.ErrorCode;
import com.crx.raf.kids.d1.util.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ResultRetrieverPool extends Pool {

    private static final Logger logger = LoggerFactory.getLogger(ResultRetrieverPool.class);
    private final Map<String, List<CompletableFuture<Result<Map<String, Integer>>>>> queryResultsMap = new ConcurrentHashMap<>();

    public ResultRetrieverPool(int poolSize) {
        super(poolSize);
    }

    public void addCorpusResult(String query, CompletableFuture<Result<Map<String, Integer>>> result) {
        if (query == null) {
            System.err.println("QUERY IS NULL?!");
            return;
        }

        List<CompletableFuture<Result<Map<String, Integer>>>> list = queryResultsMap.putIfAbsent(query, new ArrayList<>()); // TODO: reconsider?
        if (list == null) {
            list = queryResultsMap.get(query);
        }
        list.add(result);
    }


    public Map<String, Integer> getResult(String query) {
        // also check if there are jobs in queue for query

        List<CompletableFuture<Result<Map<String, Integer>>>> list = queryResultsMap.get(query);

        if (list == null) {
            logger.warn("List is empty for query: {}", query);
            return null;
        }

        long unfinishedJobs = list.stream().filter(cf -> !cf.isDone()).count();

        if (unfinishedJobs > 0) {
            logger.warn("{} Jobs are still running.", unfinishedJobs);
            return null;
        }

        logger.info("Started collecting jobs by query {}", query);

        List<Map<String, Integer>> results = list.stream()
                .map(cf -> {
                    try {
                        return cf.get();
                    } catch (Exception e) {
                        return Result.<Map<String, Integer>>error(Error.of(ErrorCode.JOB_ERROR, e.getMessage()));
                    }
                })
                .filter(r -> {
                    if (r.isError()) {
                        logger.warn("Job error: {}",r.getError());
                        return false;
                    }
                    return true;
                })
                .map(Result::getValue).collect(Collectors.toList());

        logger.info("Finished collecting jobs by query {}", query);

        Map<String, Integer> result = new HashMap<>();

        for (Map<String, Integer> r : results) {
            r.forEach((k ,v) -> {
                Integer i = result.getOrDefault(k, 0);
                result.put(k, i + v);
            });
        }

        logger.info("Query RESULT: {}", result);

        return result;
    }

    public Map<String, Integer> queryResult(String query) {
        return null;
    }

    public void clearSummary(ScanType summaryType) {

    }

    public Map<String, Map<String, Integer>> getSummary(ScanType summaryType) {
        return null;
    }
    public Map<String, Map<String, Integer>> querySummary(ScanType summaryType) {
        return null;
    }
//    public void addCorpusResult(String corpusName, Future<Map<String, Integer>> corpusResult);



}
