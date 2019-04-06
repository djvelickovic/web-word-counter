package com.crx.raf.kids.d1.result;

import com.crx.raf.kids.d1.Pool;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Error;
import com.crx.raf.kids.d1.util.ErrorCode;
import com.crx.raf.kids.d1.util.Result;
import com.crx.raf.kids.d1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

public class ResultRetrieverPool extends Pool {

    private static final Logger logger = LoggerFactory.getLogger(ResultRetrieverPool.class);

    private final ConcurrentMap<String, ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>>> queryResultsMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentLinkedQueue<Map<String, Integer>>> calculatedResults = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, Map<String, Integer>> storedResults = new ConcurrentHashMap<>();


    boolean calculateNext(String query) {
        ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>> queue = queryResultsMap.get(query);
        if (queue == null) {
            return false;
        }
        CompletableFuture<Result<Map<String, Integer>>> cf = queue.poll();
        if (cf == null) {
            return false;
        }
        try {
            Result<Map<String, Integer>> result = cf.get();

            if (result.isError()) {
                logger.error("{}", result);
                return false;
            }
            ConcurrentLinkedQueue<Map<String, Integer>> resultQueue = calculatedResults.putIfAbsent(query, new ConcurrentLinkedQueue<>());
            if (resultQueue == null){
                resultQueue = calculatedResults.get(query);
            }
            resultQueue.add(result.getValue());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


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


    private boolean updateStoredResults(String query) {
        ConcurrentLinkedQueue<Map<String, Integer>> partialResultQueue = calculatedResults.get(query);
        if (partialResultQueue == null) {
            return false;
        }

        Map<String, Integer> partialResult = partialResultQueue.poll();
        if (partialResult == null) {
            return false;
        }

        storedResults.putIfAbsent(query, new HashMap<>());
        storedResults.computeIfPresent(query, (key, value) -> {
            return Util.addMaps(value, partialResult);
        });
        return true;
    }

    public Map<String, Integer> getResult(String query) {
        // also check if there are jobs in queue for query

        while (calculateNext(query));
        while (updateStoredResults(query));

        return storedResults.get(query);
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

        // this will not be a blocking part because jobs are already checked that are done
        while (calculateNext(query));
        while (updateStoredResults(query));

        return Result.of(storedResults.get(query));
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
