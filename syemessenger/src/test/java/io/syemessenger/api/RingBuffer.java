package io.syemessenger.api;

import java.util.concurrent.atomic.AtomicLong;

public class RingBuffer<T> {

  private final Object[] objects;
  private final AtomicLong writePosition = new AtomicLong();

  public RingBuffer(int capacity) {
    objects = new Object[capacity];
  }

  public void offer(T object) {
    final int index = (int) (writePosition.getAndIncrement() % objects.length);
    objects[index] = object;
  }

  public Object[] objects() {
    return objects;
  }

  public long writePosition() {
    return writePosition.get();
  }
}
