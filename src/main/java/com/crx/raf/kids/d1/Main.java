package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.files.DirectoryCrawler;
import com.crx.raf.kids.d1.files.FileScannerPool;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.job.ScanType;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;
import com.crx.raf.kids.d1.util.Result;
import com.crx.raf.kids.d1.web.WebJob;
import com.crx.raf.kids.d1.web.WebScannerPool;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        JobQueue jobQueue = new JobQueue();

        ResultRetrieverPool resultRetrieverPool = new ResultRetrieverPool(Config.get().getResultRetrieverThreadPool());
        FileScannerPool fileScannerPool = new FileScannerPool(jobQueue, resultRetrieverPool, Config.get().getFileScannerThreadPool());
        WebScannerPool webScannerPool = new WebScannerPool(jobQueue, resultRetrieverPool, Config.get().getWebScannerThreadPool());

        DirectoryCrawler directoryCrawler = new DirectoryCrawler(jobQueue);
        new Thread(directoryCrawler).start();

        JobDispatcher jobDispatcher = new JobDispatcher(webScannerPool, fileScannerPool, jobQueue);
        new Thread(jobDispatcher).start();

        Scanner scanner = new Scanner(System.in);
        while (true) {

            try {
                String input = scanner.nextLine();
                String[] tokens = input.split("\\s+");
                String[] subTokens;

                switch (tokens[0]) {
                    case "aw":
                        WebJob webJob = new WebJob(tokens[1], Config.get().getHopCount(), jobQueue);
                        jobQueue.add(webJob);
                        break;
                    case "get":
                        subTokens = tokens[1].split("\\|");
                        if (subTokens[1].equals("summary")) {
                            printSummary(resultRetrieverPool.getSummary(ScanType.valueOf(subTokens[0].toUpperCase())));
                        }
                        else {
                            printResult(resultRetrieverPool.getResult(tokens[1]));
                        }
                        break;
                    case "query":
                        subTokens = tokens[1].split("\\|");
                        if (subTokens[1].equals("summary")) {
                            printSummary(resultRetrieverPool.querySummary(ScanType.valueOf(subTokens[0].toUpperCase())));
                        }
                        else {
                            printResult(resultRetrieverPool.queryResult(tokens[1]));
                        }
                        break;
                    case "ad":
                        directoryCrawler.addDir(tokens[1]);
                        break;
                    case "cws":
                        resultRetrieverPool.clearSummary(ScanType.WEB);
                        break;
                    case "cfs":
                        resultRetrieverPool.clearSummary(ScanType.FILE);
                        break;
                    case "exit":
                        // shutdown application
                        // run = false;
                        System.exit(1); // remove, shutdown gracefully
                        break;
                    default:
                        System.out.println("Unknown command: "+tokens[0]);
                        break;
                }
            }
            catch (Exception e){
                System.err.println("Invalid input!");
            }
        }
    }

    public static void printSummary(Result<Map<String,Map<String, Integer>>> result) {
        if (result.isError()) {
            System.err.println(result.getError().getErrorCode()+" - "+result.getError().getMessage());
        }
        result.getValue().forEach((k ,v) -> System.out.println(k+" - "+v));
    }

    public static void printResult(Result<Map<String, Integer>> result) {
        if (result.isError()) {
            System.err.println(result.getError().getErrorCode()+" - "+result.getError().getMessage());
        }
        System.out.println(result.getValue());
    }
}
