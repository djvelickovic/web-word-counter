package com.crx.raf.kids.d1.web;

import com.crx.raf.kids.d1.job.Job;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.job.ScanType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class WebJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WebJob.class);

    private static final Set<String> deduplicier = ConcurrentHashMap.newKeySet();

    private final String uri;
    private String host;
    private final int hops;
    private final JobQueue jobQueue;

    private final Set<String> keywords;

    private boolean error = false;
    public WebJob(Set<String> keywords, String uri, int hops, JobQueue jobQueue) {
        this.uri = uri;
        this.hops = hops;
        this.jobQueue = jobQueue;
        this.keywords = keywords;

        try {
            this.host = new URI(uri).getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            error = true;
        }
    }

    @Override
    public ScanType getType() {
        return ScanType.WEB;
    }

    @Override
    public String getQuery() {
        return "web|"+host;
    }

    @Override
    public CompletableFuture<Map<String, Integer>> initiate(Executor executorService) {
        if (hops < 0) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        return CompletableFuture.supplyAsync(this::job, executorService);
    }

    private Map<String, Integer> job() {

        Map<String, Integer> map = keywords.stream().collect(Collectors.toMap(t -> t, t-> 0));

        try {
//            String url = "https://www.google.com/";
            if (error || uri.endsWith(".pdf") || uri.endsWith(".jpg") || uri.endsWith(".docx") || uri.endsWith(".rar") ||  deduplicier.contains(uri)){
                logger.info("Skipping {}...", uri);
                return new HashMap<>();
            }
            else {
                deduplicier.add(uri);
            }

            logger.info("Fetching {}...", uri);

            Document doc = Jsoup.connect(uri).get();
            Elements links = doc.select("a[href]");

            logger.info("Links size: ({})", links.size());
            for (Element link : links) {
                String l = link.attr("abs:href");
                jobQueue.add(new WebJob(keywords, l, hops - 1, jobQueue));
                logger.info("Link: ({})", l);
            }


//            Arrays.stream(doc.body().text().trim().split("\\s+")).filter(words::contains).map()

            String[] words = doc.body().text().trim().split("\\s+");

            for (String word : words) {
                if (keywords.contains(word)) {
                    Integer i = map.get(word);
                    map.put(word, i + 1);
                }
            }
            return map;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return map;
    }
}
