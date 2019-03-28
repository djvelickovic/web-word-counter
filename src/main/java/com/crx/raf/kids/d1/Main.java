package com.crx.raf.kids.d1;

import com.crx.raf.kids.d1.files.FileScannerPool;
import com.crx.raf.kids.d1.job.JobQueue;
import com.crx.raf.kids.d1.result.ResultRetrieverPool;
import com.crx.raf.kids.d1.web.WebJob;
import com.crx.raf.kids.d1.web.WebScannerPool;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        boolean run = true;

        DirectoryCrawler directoryCrawler = new DirectoryCrawler();
        JobQueue jobQueue = new JobQueue();

        ResultRetrieverPool resultRetrieverPool = new ResultRetrieverPool();


        FileScannerPool fileScannerPool = new FileScannerPool(jobQueue, resultRetrieverPool, 5);
        WebScannerPool webScannerPool = new WebScannerPool(jobQueue, resultRetrieverPool, 5);

        JobDispatcher jobDispatcher = new JobDispatcher(webScannerPool, fileScannerPool, jobQueue);

        new Thread(jobDispatcher).start();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();

            String[] tokens = input.split("\\s+");

            switch (tokens[0]) {
                case "aw":
                    WebJob webJob = new WebJob(tokens[1], 1, jobQueue);
                    jobQueue.add(webJob);
                    break;
                case "ad":
                    break;
                case "exit":
                    // shutdown application
                    // run = false;
                    System.exit(1); // remove, shutdown gracefully
                    break;
            }
        }

    }


}
