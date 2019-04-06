package com.crx.raf.kids.d1.files;

import com.crx.raf.kids.d1.Config;
import com.crx.raf.kids.d1.job.JobQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DirectoryCrawler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(DirectoryCrawler.class);

    private JobQueue jobQueue;
    private boolean run = true;

    private List<String> dirs = new CopyOnWriteArrayList<>();
    private Map<String, Map<String, Long>> filesPerCorpus = new HashMap<>();
    private Map<String, Boolean> corpusesForCheck = new HashMap<>();

    @Override
    public void run() {
        while (run) {
            try {
                Thread.sleep(Config.get().getDirCrawlerSleepTime());

                Iterator<String> dirIterator = dirs.listIterator();

                while (dirIterator.hasNext()) {
                    String dir = dirIterator.next();
                    File file = new File(dir);
                    traverseFiles(file);
                }

                corpusesForCheck.forEach((k, v) -> {
                    System.out.println(k +"  "+v);


                });

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }




    private void traverseFiles(File root) {
        if (root.isDirectory()) {
            boolean isCorpus = root.getName().startsWith(Config.get().getFileCorpusPrefix());
            if (isCorpus) {
                Map<String, Long> lastTimeModifiedPerFileName = filesPerCorpus.getOrDefault(root.getName(), new HashMap<>());
                for (File child : root.listFiles()) {
                    long lastTimeModified = lastTimeModifiedPerFileName.getOrDefault(child.getName(), 0L);
                    if (lastTimeModified != child.lastModified()) {
                        corpusesForCheck.put(root.getName(), true);
                        break;
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
