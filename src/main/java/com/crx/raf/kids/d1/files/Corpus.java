package com.crx.raf.kids.d1.files;

public class Corpus {
    private String name;
    private long lastModified;

    public Corpus(String name, long lastModified) {
        this.name = name;
        this.lastModified = lastModified;
    }

    public String getName() {
        return name;
    }

    public long getLastModified() {
        return lastModified;
    }
}
