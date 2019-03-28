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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class WebJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(WebJob.class);

    private final String uri;
    private String host;
    private final int hops;
    private final JobQueue jobQueue;

    public WebJob(String uri, int hops, JobQueue jobQueue) {
        this.uri = uri;
        this.hops = hops;
        this.jobQueue = jobQueue;

        try {
            this.host = new URI(uri).getHost();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public ScanType getType() {
        return ScanType.WEB;
    }

    @Override
    public String getQuery() {
        return "web|"+uri;
    }

    @Override
    public CompletableFuture<Map<String, Integer>> initiate(Executor executorService) {
        if (hops < 0) {
            return CompletableFuture.completedFuture(new HashMap<>());
        }
        return CompletableFuture.supplyAsync(this::job, executorService);
    }

    private Map<String, Integer> job() {
        try {
//            String url = "https://www.google.com/";

            logger.info("Fetching {}...", uri);

            Document doc = Jsoup.connect(uri).get();
            Elements links = doc.select("a[href]");

            logger.info("Links size: ({})", links.size());
            for (Element link : links) {
                String l = link.attr("abs:href");
                jobQueue.add(new WebJob(l, hops - 1, jobQueue));
                logger.info("Link: ({})", l);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return new HashMap<>();
    }
}
