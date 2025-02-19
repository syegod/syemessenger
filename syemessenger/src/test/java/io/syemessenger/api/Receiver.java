package io.syemessenger.api;

import java.util.function.Predicate;

public class Receiver {

  private final RingBuffer<ServiceMessage> ringBuffer;
  private long readPosition;

  public Receiver(RingBuffer<ServiceMessage> ringBuffer) {
    this.ringBuffer = ringBuffer;
  }

  public ServiceMessage poll(Predicate<ServiceMessage> predicate) {
    final var objects = ringBuffer.objects();
    final var writePosition = ringBuffer.writePosition();
    final var length = objects.length;

    if (writePosition - readPosition > length + 1) {
      throw new RuntimeException(
          "Poll failed: write position: " + writePosition + ", read position: " + readPosition);
    }

    if (readPosition == writePosition) {
      return null;
    }

    final var r = readPosition;
    final int index = (int) (readPosition++ % length);
    final var result = (ServiceMessage) objects[index];

    if (ringBuffer.writePosition() - r > length + 1) {
      throw new RuntimeException(
          "Poll failed: write position: " + writePosition + ", read position: " + readPosition);
    }

    if (predicate.test(result)) {
      return result;
    }

    return null;
  }
}
