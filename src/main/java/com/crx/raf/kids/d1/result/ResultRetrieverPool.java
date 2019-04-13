package com.crx.raf.kids.d1.result;

import com.crx.raf.kids.d1.Config;
import com.crx.raf.kids.d1.Pool;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Error;
import com.crx.raf.kids.d1.util.ErrorCode;
import com.crx.raf.kids.d1.util.Result;
import com.crx.raf.kids.d1.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ResultRetrieverPool extends Pool {

    private static final Logger logger = LoggerFactory.getLogger(ResultRetrieverPool.class);

    private final ConcurrentMap<String, CompletableFuture<Result<Map<String, Integer>>>> webJobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Collection<CompletableFuture<Result<Map<String, Integer>>>>> fileJobs = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Integer> corpusVersions = new ConcurrentHashMap<>();

//    private final ConcurrentMap<String, ConcurrentLinkedQueue<CompletableFuture<Result<Map<String, Integer>>>>> queryResultsMap = new ConcurrentHashMap<>();
//    private final ConcurrentMap<String, ConcurrentLinkedQueue<Map<String, Integer>>> calculatedResults = new ConcurrentHashMap<>();

    private ConcurrentMap<String, CompletableFuture<Void>> jobsByQuery = new ConcurrentHashMap<>();

    private ConcurrentMap<String, Map<String, Integer>> storedResults = new ConcurrentHashMap<>();

    private final Set<String> keywords = new HashSet<>(Arrays.asList(Config.get().getKeywords()));

    public ResultRetrieverPool(int poolSize) {
        super(poolSize);
    }

    public void addCorpusResult(String query, CompletableFuture<Result<Map<String, Integer>>> result) {
        if (query == null) {
            System.err.println("QUERY IS NULL?!");
            return;
        }

        if (query.startsWith("web")) {
            webJobs.put(query, result);
        }
        else if (query.startsWith("file")) {
            String[] tokens = query.split("\\|");
            int corpusVersion = Integer.parseInt(tokens[2]);

            corpusVersions.put(tokens[1], corpusVersion);

            Collection<CompletableFuture<Result<Map<String, Integer>>>> fileJobCollectionPerVersion = fileJobs.putIfAbsent(query, new ConcurrentLinkedQueue<>());
            if (fileJobCollectionPerVersion == null){
                fileJobCollectionPerVersion = fileJobs.get(query);
            }
            fileJobCollectionPerVersion.add(result);
        }
    }

    public Result<Map<String, Integer>> getResult(String query) {
//        final ConcurrentMap<String, CompletableFuture<Void>> jobsByQuery = this.jobsByQuery;
        final ConcurrentMap<String, Map<String, Integer>> storedResults = this.storedResults;

        CompletableFuture<Void> job = jobsByQuery.putIfAbsent(query, initiateCalculationForQuery(query, jobsByQuery, storedResults));
        if (job == null) {
            job = jobsByQuery.get(query);
        }
        try {
            job.get();
            return Result.of(storedResults.get(query));
        } catch (Exception e) {
            return Result.error(Error.of(ErrorCode.RESULT_RETRIEVER_SUMMARY_ERROR, e.getMessage()));
        }
    }

    public Result<Map<String, Integer>> queryResult(String query) {
        final ConcurrentMap<String, CompletableFuture<Void>> jobsByQuery = this.jobsByQuery;
        final ConcurrentMap<String, Map<String, Integer>> storedResults = this.storedResults;

        CompletableFuture<Void> job = jobsByQuery.putIfAbsent(query, initiateCalculationForQuery(query, jobsByQuery, storedResults));
        if (job == null) {
            job = jobsByQuery.get(query);
        }
        if (job.isDone()) {
            return Result.of(storedResults.get(query));
        }
        return Result.error(Error.of(ErrorCode.RESULTS_ARE_NOT_AVAILABLE_YET, ""));
    }

    public void clearSummary(ScanType summaryType) {
        storedResults = storedResults.entrySet()
                .stream()
                .filter(e -> !e.getKey().startsWith(summaryType.name().toLowerCase()))
                .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> e.getValue()));

        jobsByQuery = new ConcurrentHashMap<>();
    }

    public Result<Map<String, Map<String, Integer>>> getSummary(ScanType scanType) {
        final ConcurrentMap<String, CompletableFuture<Void>> jobsByQuery = this.jobsByQuery;
        final ConcurrentMap<String, Map<String, Integer>> storedResults = this.storedResults;

        if (scanType == ScanType.FILE) {
            webJobs.keySet().stream()
                    .filter(query -> query.startsWith(scanType.name().toLowerCase()))
                    .filter(query -> !jobsByQuery.containsKey(query))
                    .forEach(query -> jobsByQuery.putIfAbsent(query, initiateCalculationForQuery(query, jobsByQuery, storedResults)));
        }
        else if (scanType == ScanType.WEB) {
            fileJobs.keySet().stream()
                    .filter(query -> query.startsWith(scanType.name().toLowerCase()))
                    .filter(query -> !jobsByQuery.containsKey(query))
                    .forEach(query -> jobsByQuery.putIfAbsent(query, initiateCalculationForQuery(query, jobsByQuery, storedResults)));
        }

        jobsByQuery.forEach((k, v) -> {
            try {
                v.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return Result.of(storedResults.entrySet().stream()
                .filter(e -> e.getKey().startsWith(scanType.name().toLowerCase()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public Result<Map<String, Map<String, Integer>>> querySummary(ScanType scanType) {
        final ConcurrentMap<String, CompletableFuture<Void>> jobsByQuery = this.jobsByQuery;
        final ConcurrentMap<String, Map<String, Integer>> storedResults = this.storedResults;

        if (scanType == ScanType.FILE) {
            webJobs.keySet().stream()
                    .filter(query -> query.startsWith(scanType.name().toLowerCase()))
                    .filter(query -> !jobsByQuery.containsKey(query))
                    .forEach(query -> jobsByQuery.putIfAbsent(query, initiateCalculationForQuery(query, jobsByQuery, storedResults)));
        }
        else if (scanType == ScanType.WEB) {
            fileJobs.keySet().stream()
                    .filter(query -> query.startsWith(scanType.name().toLowerCase()))
                    .filter(query -> !jobsByQuery.containsKey(query))
                    .forEach(query -> jobsByQuery.putIfAbsent(query, initiateCalculationForQuery(query, jobsByQuery, storedResults)));
        }

        if (!jobsByQuery.entrySet().stream().allMatch(e -> e.getValue().isDone())) {
            return Result.error(Error.of(ErrorCode.RESULTS_ARE_NOT_AVAILABLE_YET, ""));
        }

        return Result.of(storedResults.entrySet().stream()
                .filter(e -> e.getKey().startsWith(scanType.name().toLowerCase()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    public CompletableFuture<Void> initiateCalculationForQuery(String query, final ConcurrentMap<String, CompletableFuture<Void>> jobsByQuery, final ConcurrentMap<String, Map<String, Integer>> storedResults) {

        if (jobsByQuery.containsKey(query)) {
            return CompletableFuture.completedFuture(null);
        }

        String[] tokens = query.split("\\|");
        String corpusOrDomain = tokens[1];

        Queue<CompletableFuture<Result<Map<String, Integer>>>> queue = new LinkedList<>();

        if (query.startsWith("web")) {
            webJobs.entrySet()
                    .stream()
                    .filter(e -> e.getKey().contains(corpusOrDomain))
                    .forEach(e -> queue.add(e.getValue()));
        }
        else if (query.startsWith("file")) {
            Integer corpusVersion = corpusVersions.get(corpusOrDomain);
            if (corpusVersion == null) {
                System.out.println("Corpus doesnt have version. Error");
                return CompletableFuture.completedFuture(null);
            }
            fileJobs.entrySet()
                    .stream()
                    .filter(e -> e.getKey().equals(query+"|"+corpusVersion))
                    .forEach(e -> queue.addAll(e.getValue()));
        }

        return CompletableFuture.supplyAsync(() -> {
            while (true) {
                CompletableFuture<Result<Map<String, Integer>>> cf = queue.poll();
                if (cf == null) {
                    return null;
                }
                try {
                    Result<Map<String, Integer>> result = cf.get();
                    if (result.isError()) {
                        System.err.println(result);
                        continue;
                    }
                    storedResults.compute(query, (key, value) -> {
                        if (value == null) {
                            value = Util.generateCleanMap(keywords);
                        }
                        return Util.addMaps(value, result.getValue(), keywords);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, executorService);
    }
}
