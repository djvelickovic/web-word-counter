package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.files.DirectoryCrawler;
import com.crx.raf.kids.d1.files.FileScannerPool;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;
import com.crx.raf.kids.d1.web.WebJob;
import com.crx.raf.kids.d1.web.WebScannerPool;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Main {

    public static void main(String[] args) {

        boolean run = true;

        DirectoryCrawler directoryCrawler = new DirectoryCrawler();
        new Thread(directoryCrawler).start();

        JobQueue jobQueue = new JobQueue();

        ResultRetrieverPool resultRetrieverPool = new ResultRetrieverPool(Config.get().getResultRetrieverThreadPool());

        FileScannerPool fileScannerPool = new FileScannerPool(jobQueue, resultRetrieverPool, Config.get().getFileScannerThreadPool());
        WebScannerPool webScannerPool = new WebScannerPool(jobQueue, resultRetrieverPool, Config.get().getWebScannerThreadPool());

        JobDispatcher jobDispatcher = new JobDispatcher(webScannerPool, fileScannerPool, jobQueue);

        new Thread(jobDispatcher).start();

        Scanner scanner = new Scanner(System.in);

        Set<String> keywords = new HashSet<>(Arrays.asList(Config.get().getKeywords()));

        while (true) {

            try {
                String input = scanner.nextLine();
                String[] tokens = input.split("\\s+");

                switch (tokens[0]) {
                    case "aw":
                        WebJob webJob = new WebJob(keywords, tokens[1], 1, jobQueue);
                        jobQueue.add(webJob);
                        break;
                    case "get":
                        System.out.println(resultRetrieverPool.getResult(tokens[1]).toString());
                        break;
                    case "query":
                        System.out.println(resultRetrieverPool.queryResult(tokens[1]).toString());
                        break;
                    case "ad":
                        directoryCrawler.addDir(tokens[1]);
                        break;
                    case "exit":
                        // shutdown application
                        // run = false;
                        System.exit(1); // remove, shutdown gracefully
                        break;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                System.err.println("Invalid input!");
            }
        }
    }
}
