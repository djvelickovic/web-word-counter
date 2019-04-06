package com.crx.raf.kids.d1;

import java.io.IOException;
import java.util.Properties;

public class Config {

    private static final Config config = new Config();

    public static Config get() {
        return config;
    }

    private String[] keywords = {"one", "two", "three", "raf", "RAF"};
    private String fileCorpusPrefix = "corpus_";
    private int dirCrawlerSleepTime = 1000;
    private long fileScanningSizeLimit = 1048576;
    private int hopCount = 1;
    private long urlRefreshTime = 86400000;
    private int webScannerThreadPool = 10;
    private int fileScannerThreadPool = 10;
    private int resultRetrieverThreadPool = 10;


    private Config() {
        try {
            Properties properties = new Properties();
            //load a properties file from class path, inside static method
            properties.load(Config.class.getClassLoader().getResourceAsStream("config.properties"));

            //get the property value and print it out
            keywords = properties.getProperty("keywords").split(",");
            fileCorpusPrefix = properties.getProperty("file_corpus_prefix");
            dirCrawlerSleepTime = Integer.parseInt(properties.getProperty("dir_crawler_sleep_time"));
            fileScanningSizeLimit = Long.parseLong(properties.getProperty("file_scanning_size_limit"));
            hopCount = Integer.parseInt(properties.getProperty("hop_count"));
            urlRefreshTime = Long.parseLong(properties.getProperty("url_refresh_time"));

            webScannerThreadPool = Integer.parseInt(properties.getProperty("web_scanner_pool"));
            fileScannerThreadPool = Integer.parseInt(properties.getProperty("file_scanner_pool"));
            resultRetrieverThreadPool = Integer.parseInt(properties.getProperty("result_retriever_pool"));
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public String[] getKeywords() {
        return keywords;
    }

    public String getFileCorpusPrefix() {
        return fileCorpusPrefix;
    }

    public int getDirCrawlerSleepTime() {
        return dirCrawlerSleepTime;
    }

    public long getFileScanningSizeLimit() {
        return fileScanningSizeLimit;
    }

    public int getHopCount() {
        return hopCount;
    }

    public long getUrlRefreshTime() {
        return urlRefreshTime;
    }

    public int getWebScannerThreadPool() {
        return webScannerThreadPool;
    }

    public int getFileScannerThreadPool() {
        return fileScannerThreadPool;
    }

    public int getResultRetrieverThreadPool() {
        return resultRetrieverThreadPool;
    }
}
