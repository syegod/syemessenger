package io.syemessenger.api;

import java.util.concurrent.atomic.AtomicLong;

public class RingBuffer<T> {

  private final Object[] objects;
  private final AtomicLong writePosition = new AtomicLong();

  public RingBuffer(int capacity) {
    objects = new Object[capacity];
  }

  public void offer(T object) {
    final int index = (int) (writePosition.get() % objects.length);
    objects[index] = object;
    writePosition.incrementAndGet();
  }

  public Object[] objects() {
    return objects;
  }

  public long writePosition() {
    return writePosition.get();
  }
}
