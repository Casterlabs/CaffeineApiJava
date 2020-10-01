package co.casterlabs.caffeineapi;

import lombok.SneakyThrows;

public class ThreadHelper {

    public static Thread executeAsync(Runnable run) {
        Thread t = new Thread(run);

        t.start();

        return t;
    }

    public static Thread executeAsyncDaemon(Runnable run) {
        Thread t = new Thread(run);

        t.setDaemon(true);
        t.start();

        return t;
    }

    public static Thread executeAsyncLater(Runnable run, long millis) {
        Thread t = (new Thread() {
            @SneakyThrows
            @Override
            public void run() {
                Thread.sleep(millis);
                run.run();
            }
        });

        t.setDaemon(true);
        t.start();

        return t;
    }

}
