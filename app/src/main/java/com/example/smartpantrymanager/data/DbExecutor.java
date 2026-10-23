package com.example.smartpantrymanager.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Room does not allow database work on the main (UI) thread.
 * Every database call in the app goes through this single background thread,
 * which also guarantees the jobs run in the order they were submitted
 * (so the recipes are always seeded before they are read).
 */
public final class DbExecutor {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private DbExecutor() {
    }

    public static void run(Runnable job) {
        EXECUTOR.execute(job);
    }
}
