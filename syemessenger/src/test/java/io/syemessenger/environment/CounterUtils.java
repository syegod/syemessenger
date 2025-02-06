package io.syemessenger.environment;

import java.util.concurrent.atomic.AtomicLong;

public class CounterUtils {

  private static final AtomicLong LONG_COUNTER = new AtomicLong((long) 1e3);

  private CounterUtils() {}

  public static long nextLong() {
    return LONG_COUNTER.incrementAndGet();
  }
}
