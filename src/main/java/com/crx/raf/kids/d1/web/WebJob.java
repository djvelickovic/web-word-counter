package com.crx.raf.kids.d1.web;

import com.crx.raf.kids.d1.Config;
import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.util.Error;
import com.crx.raf.kids.d1.util.ErrorCode;
import com.crx.raf.kids.d1.util.Result;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WebJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WebJob.class);
    private static final ConcurrentMap<String, Long> doneWebJobQueries = new ConcurrentHashMap<>();

    private final String uri;
    private final int hops;
    private final JobQueue jobQueue;

    private final Set<String> keywords;

    public WebJob(String uri, int hops, JobQueue jobQueue) {
        this.uri = uri;
        this.hops = hops;
        this.jobQueue = jobQueue;
        this.keywords = new HashSet<>(Arrays.asList(Config.get().getKeywords()));
    }

    @Override
    public ScanType getType() {
        return ScanType.WEB;
    }

    @Override
    public Result<String> getQuery() {
        return Result.of("web|"+uri);
    }

    @Override
    public Result<CompletableFuture<Result<Map<String, Integer>>>> initiate(Executor executorService) {
        try {
            Long millis = doneWebJobQueries.putIfAbsent(uri, System.currentTimeMillis());

            if (millis == null) { // uri haven't added before
                return Result.of(CompletableFuture.supplyAsync(this::doJob, executorService));
            }

            millis = doneWebJobQueries.get(uri);

            if (System.currentTimeMillis() - millis < Config.get().getUrlRefreshTime()) { // uri haven't expired
                logger.info("Skipping job {}", uri);
                return Result.error(Error.of(ErrorCode.WEB_URL_VISITED, ""));
            }
            else { // uri has expired. clean it
                doneWebJobQueries.remove(uri);
            }
            // retry
            return initiate(executorService);
        }
        catch (Exception e){
            return Result.error(Error.of(ErrorCode.CRAWLER_ERROR, e.getMessage()));
        }
    }

    private Result<Map<String, Integer>> doJob() {
        try {
            if (uri.endsWith(".pdf") || uri.endsWith(".jpg") || uri.endsWith(".docx") || uri.endsWith(".rar")){
                logger.info("Skipping. Format unsupported. {}...", uri);
                return Result.of(new HashMap<>());
            }

            logger.info("Fetching {}...", uri);

            Document doc = Jsoup.connect(uri).get();
            Elements links = doc.select("a[href]");


            if (hops > 0) {
                logger.info("Links size: ({})", links.size());
                for (Element link : links) {
                    String l = link.attr("abs:href");
                    if (l.startsWith("http")) {
                        jobQueue.add(new WebJob(l, hops - 1, jobQueue));
                        logger.info("Link: ({})", l);
                    } else {
                        logger.error("UNKNOWN PROTOCOL: {}", l);
                    }
                }
            }

            String[] words = doc.body().text().trim().split("\\s+");
            Map<String, Integer> map = keywords.stream().collect(Collectors.toMap(keyword -> keyword, keyword -> count(keyword, words)));
            logger.info("RESULT for QUERY {}: {}", getQuery(), map);
            return Result.of(map);
        }
        catch (Exception e){
            return Result.error(Error.of(ErrorCode.CRAWLER_ERROR, e.getMessage()));
        }
    }

    private int count(String keyword, String[] words) {
        return (int) Stream.of(words).filter(word -> word.equals(keyword)).count();
    }

}
