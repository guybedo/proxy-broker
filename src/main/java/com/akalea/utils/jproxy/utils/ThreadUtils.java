package com.akalea.utils.jproxy.utils;

import java.util.Collection;

public class ThreadUtils {

  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
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
