package com.crx.raf.kids.d1.files;

import com.crx.raf.kids.d1.Config;
import com.crx.raf.kids.d1.job.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryCrawler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryCrawler.class);

    private JobQueue jobQueue;
    private volatile boolean run = true;

    private List<String> dirs = new CopyOnWriteArrayList<>();

    private Map<String, Long> corpusLastChanged = new HashMap<>();
    private Map<String, File> corpusFile = new HashMap<>();
    private Set<String> corpusesForCheck = new HashSet<>();
    private Map<String, Integer> corpusesIterations = new HashMap<>();

    public DirectoryCrawler(JobQueue jobQueue) {
        this.jobQueue = jobQueue;
    }

    @Override
    public void run() {
        while (run) {
            try {
                Thread.sleep(Config.get().getDirCrawlerSleepTime());
                Iterator<String> dirIterator = dirs.listIterator();

//                corpusFile.clear();

                while (dirIterator.hasNext()) {
                    String dir = dirIterator.next();
                    File file = new File(dir);
                    traverseFiles(file);
                }

                corpusesForCheck.forEach(corpusName -> {
                            System.out.println("Checking corpus: "+corpusName);
                            File[] files = corpusFile.get(corpusName).listFiles();
                            int corpusIteration = corpusesIterations.compute(corpusName, (k, v)-> {
                                if (v == null) {
                                    return 1;
                                }
                                return v+1;
                            });
                            dispatchJobs(corpusName, files, corpusIteration);
                        });

                corpusesForCheck.clear();

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Directory crawler stopped...");
    }

    void dispatchJobs(String corpus, File[] files, int corpusVersion) {
        List<File> filesForJob = new ArrayList<>();
        long sum = 0L;
        for (File f : files) {
            if (f.isDirectory()) {
                continue;
            }

            sum += f.length();
            filesForJob.add(f);

            if (sum > Config.get().getFileScanningSizeLimit()) {
                jobQueue.add(new FileJob(filesForJob, corpus, corpusVersion));
                sum = 0L;
                filesForJob = new ArrayList<>();
            }
        }
        if (!filesForJob.isEmpty()) {
            jobQueue.add(new FileJob(filesForJob, corpus, corpusVersion));
        }
    }

    private void traverseFiles(File root) {
        if (root.isDirectory()) {
            boolean isCorpus = root.getName().startsWith(Config.get().getFileCorpusPrefix());
            if (isCorpus) {
                corpusFile.put(root.getName(), root);
                if (!corpusLastChanged.containsKey(root.getName())) {
                    corpusLastChanged.put(root.getName(), root.lastModified());
                    corpusesForCheck.add(root.getName());
                }
                else {
                    Long lastChanged = corpusLastChanged.get(root.getName());
                    if (lastChanged != root.lastModified()) {
                        corpusesForCheck.add(root.getName());
                        corpusLastChanged.put(root.getName(), root.lastModified());

                    }
                }
            }
            else {
                for (File child : root.listFiles()) {
                    traverseFiles(child);
                }
            }
        }
    }

    public void addDir(String dir) {
        dirs.add(dir);
    }

    public void stop() {
        run = false;
    }
}
