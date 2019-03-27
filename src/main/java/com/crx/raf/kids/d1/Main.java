package com.crx.raf.kids.d1;

public class Main {

    public static void main(String[] args) {

//        WebScanner scanner = new WebScanner();
//        scanner.job();

        CLI cli = new CLI();
        new Thread(cli).start();
    }



}
