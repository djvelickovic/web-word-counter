package com.crx.raf.kids.d1;

import java.util.Scanner;

public class CLI implements Runnable {

    @Override
    public void run() {

        Scanner scanner = new Scanner(System.in);

        while (true) {
            String input = scanner.nextLine();

            if (input.equalsIgnoreCase("start")) {
                // STH
            }


            if (input.equalsIgnoreCase("exit")) {
                break;
            }

        }

        // CLOSE ALL THREADS GRACEFULLY

    }
}
