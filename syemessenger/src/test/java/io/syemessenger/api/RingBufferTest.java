package io.syemessenger.api;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RingBufferTest {
  @Test
  void testRingBuffer() {
    final var buffer = new RingBuffer<ServiceMessage>(4);

    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));
    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));
    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));
    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));

    assertEquals(4, buffer.available());
  }

  @Test
  void testRingBufferPoll() {
    final var buffer = new RingBuffer<ServiceMessage>(2);

    final var uuid = UUID.randomUUID();
    final var message = new ServiceMessage().cid(uuid).data(randomAlphanumeric(10)).qualifier(randomAlphanumeric(5));

    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));
    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));
    buffer.offer(message);
    buffer.offer(new ServiceMessage().cid(UUID.randomUUID()));

    final var polledMessage = buffer.poll(i -> i.cid().equals(uuid));

    assertEquals(4, buffer.available());
    assertEquals(message, polledMessage);
  }
}
