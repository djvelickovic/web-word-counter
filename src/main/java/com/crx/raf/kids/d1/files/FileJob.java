package com.crx.raf.kids.d1.files;

import com.crx.raf.kids.d1.Config;
import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Result;
import com.crx.raf.kids.d1.util.Util;
import com.crx.raf.kids.d1.web.WebJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WebJob.class);

    private final List<File> files;
    private final Set<String> keywords;
    private final String corpusName;

    public FileJob(List<File> files, String corpusName) {
        this.files = files;
        this.keywords = new HashSet<>(Arrays.asList(Config.get().getKeywords()));
        this.corpusName = corpusName;
    }

    @Override
    public ScanType getType() {
        return ScanType.FILE;
    }

    @Override
    public Result<String> getQuery() {
        return Result.of("file|"+corpusName);
    }

    @Override
    public CompletableFuture<Result<Map<String, Integer>>> initiate(Executor executor) {
        return CompletableFuture.supplyAsync(() -> Result.of(files.stream()
                        .map(this::countFromFile)
                        .reduce((map1, map2) -> Util.addMaps(map1, map2, keywords))
                        .orElse(Util.generateCleanMap(keywords))), executor);
    }

    private Map<String, Integer> countFromFile(File file) {
        Map<String, Integer> result = Util.generateCleanMap(keywords);
        try (Scanner sc = new Scanner(new FileReader(file))) {
            while (sc.hasNext()) {
                String token = sc.next();
                if (keywords.contains(token)){
                    result.compute(token, (key, value) -> {
                        if (value == null){
                            return 1;
                        }
                        return value + 1;
                    });
                }
            }
        }
        catch (Exception e){
            logger.error("", e);
        }
        return result;
    }
}
