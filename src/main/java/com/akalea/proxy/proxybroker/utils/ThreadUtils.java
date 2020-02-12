package com.akalea.proxy.proxybroker.utils;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class ThreadUtils {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
        }
    }

    public static void waitUntil(
        Supplier<Boolean> evaluation,
        long maxWaitSecs,
        long evaluationIntervalMsec) {
        try {
            LocalDateTime limit =
                LocalDateTime
                    .now()
                    .plusSeconds(maxWaitSecs);
            while (!evaluation.get() && limit.isAfter(LocalDateTime.now()))
                Thread.sleep(evaluationIntervalMsec);
        } catch (InterruptedException e) {
        }
    }

    public static void shutdown(ExecutorService executor, long timeout, TimeUnit unit) {
        try {
            executor.shutdown();
            executor.awaitTermination(timeout, unit);
        } catch (Exception e) {
            return;
        }
    }

    public static boolean join(Collection<Thread> threads) {
        return threads
            .stream()
            .map(thread -> {
                try {
                    thread.join();
                    return true;
                } catch (InterruptedException e) {
                    return false;
                }
            })
            .reduce((a, b) -> a && b)
            .get();
    }
}
