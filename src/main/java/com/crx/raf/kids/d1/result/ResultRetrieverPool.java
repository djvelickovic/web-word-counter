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
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ResultRetrieverPool extends Pool {

    private static final Logger logger = LoggerFactory.getLogger(ResultRetrieverPool.class);
    private final Map<String, ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>>> queryResultsMap = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Map<String, Integer>> storedResults = new ConcurrentHashMap<>();

    public ResultRetrieverPool(int poolSize) {
        super(poolSize);
    }

    public void addCorpusResult(String query, CompletableFuture<Result<Map<String, Integer>>> result) {
        if (query == null) {
            System.err.println("QUERY IS NULL?!");
            return;
        }

        ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>> list = queryResultsMap.putIfAbsent(query, new ConcurrentLinkedQueue<>()); // TODO: reconsider?
        if (list == null) {
            list = queryResultsMap.get(query);
        }
        list.add(result);
    }

    private Map<String, Integer> updateStoredResults(String query, ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>> queue) {
        return storedResults.computeIfAbsent(query, key -> {
            Map<String, Integer> finalResult = new HashMap<>();
            while (true) {
                CompletableFuture<Result<Map<String, Integer>>> item = queue.poll();
                if (item == null) {
                    break;
                }
                try {
                    Result<Map<String, Integer>> result = item.get();
                    if (result.isError()) {
                        logger.warn("Job error: {}", result.getError());
                    }
                    else {
                        result.getValue().forEach((k ,v) -> {
                            Integer i = finalResult.getOrDefault(k, 0);
                            finalResult.put(k, i + v);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return finalResult;
        });
    }

    public Map<String, Integer> getResult(String query) {
        // also check if there are jobs in queue for query

        ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>> queue = queryResultsMap.get(query);

        logger.info("Started collecting jobs by query {}", query);
        Map<String, Integer> result = updateStoredResults(query, queue);
        logger.info("Get RESULT: {}", result);

        storedResults.put(query, result);
        return result;
    }

    public Result<Map<String, Integer>> queryResult(String query) {
        ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>> queue = queryResultsMap.get(query);

        if (queue == null) {
            logger.warn("List is empty for query: {}", query);
            return Result.error(Error.of(ErrorCode.THERE_ARE_NO_STARTED_JOBS, ""));
        }

        long unfinishedJobs = queue.stream().filter(cf -> !cf.isDone()).count();

        if (unfinishedJobs > 0) {
            logger.warn("{} Jobs are still running.", unfinishedJobs);
            return Result.error(Error.of(ErrorCode.RESULTS_ARE_NOT_AVAILABLE_YET, ""));
        }



        // count result?

        Map<String, Integer> result = updateStoredResults(query, queue);
        logger.info("Query RESULT: {}", result);



        return Result.of(result);
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
