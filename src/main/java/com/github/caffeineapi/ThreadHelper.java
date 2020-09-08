package com.github.caffeineapi;

import lombok.SneakyThrows;

public class ThreadHelper {

    public static void executeAsync(Runnable run) {
        new Thread(run).start();
    }

    public static void executeLater(long millis, Runnable run) {
        Thread t = new Thread() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(millis);
                run.run();
            }
        };

        t.setDaemon(true);
        t.start();
    }

}
