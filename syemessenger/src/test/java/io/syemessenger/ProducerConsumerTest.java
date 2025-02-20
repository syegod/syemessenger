package io.syemessenger;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.*;

import io.syemessenger.api.Receiver;
import io.syemessenger.api.RingBuffer;
import io.syemessenger.api.ServiceMessage;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ProducerConsumerTest {

  private static final int bufferCapacity = 64;

  private RingBuffer<ServiceMessage> ringBuffer;
  private Receiver receiver;

  @BeforeEach
  void setUp() {
    ringBuffer = new RingBuffer<>(bufferCapacity);
    receiver = new Receiver(ringBuffer);
  }

  @Test
  void testSuccess() {
    ServiceMessage message1 = createServiceMessage("test1");
    ServiceMessage message2 = createServiceMessage("test2");
    ServiceMessage message3 = createServiceMessage("test3");

    ringBuffer.offer(message1);
    ringBuffer.offer(message2);
    ringBuffer.offer(message3);

    ServiceMessage result1 = receiver.poll(msg -> msg.qualifier().equals(message1.qualifier()));
    assertEquals(message1, result1);

    ServiceMessage result2 = receiver.poll(msg -> msg.qualifier().equals(message2.qualifier()));
    assertEquals(message2, result2);

    ServiceMessage result3 = receiver.poll(msg -> msg.qualifier().equals(message3.qualifier()));
    assertEquals(message3, result3);
  }

  @Test
  void testSuccessSingle() {
    final var cid = UUID.randomUUID();
    ServiceMessage message = createServiceMessage(cid);

    final var polled1 = receiver.poll(m -> m.cid().equals(cid));

    assertNull(polled1);

    ringBuffer.offer(message);

    final var polled = receiver.poll(m -> m.cid().equals(cid));

    assertEquals(message, polled);
  }

  @Test
  void testPollWithNullWhenNoMessagesAvailable() {
    ServiceMessage result = receiver.poll(msg -> true);
    assertNull(result, "Should be null");
  }

  @Test
  void testOverwriteBehavior() {
    final var cid = UUID.randomUUID();
    ringBuffer.offer(createServiceMessage(cid));
    for (int i = 1; i <= bufferCapacity; i++) {
      ringBuffer.offer(createServiceMessage("test" + i));
    }

    ServiceMessage result = receiver.poll(msg -> msg.cid().equals(cid));
    assertNull(result, "Should be null");
  }

  @Test
  void testReceiverFailedWhenMessageOverwritten() {
    final var cid = UUID.randomUUID();
    ringBuffer.offer(createServiceMessage(cid));
    for (int i = 1; i <= bufferCapacity * 2 + 1; i++) {
      ringBuffer.offer(createServiceMessage(UUID.randomUUID()));
    }

    var thrown =
        assertThrows(RuntimeException.class, () -> receiver.poll(msg -> msg.cid().equals(cid)));

    assertTrue(thrown.getMessage().contains("has been overwritten"));
  }

  @Test
  void testPollByCidSuccess() {
    UUID cid1 = UUID.randomUUID();
    UUID cid2 = UUID.randomUUID();
    UUID cid3 = UUID.randomUUID();

    ServiceMessage message1 = new ServiceMessage().cid(cid1).qualifier("test1");
    ServiceMessage message2 = new ServiceMessage().cid(cid2).qualifier("test2");
    ServiceMessage message3 = new ServiceMessage().cid(cid3).qualifier("test3");

    ringBuffer.offer(message1);
    ringBuffer.offer(message2);
    ringBuffer.offer(message3);

    ServiceMessage result1 = receiver.poll(msg -> msg.cid().equals(cid1));
    assertEquals(message1, result1);

    ServiceMessage result2 = receiver.poll(msg -> msg.cid().equals(cid2));
    assertEquals(message2, result2);

    ServiceMessage result3 = receiver.poll(msg -> msg.cid().equals(cid3));
    assertEquals(message3, result3);
  }

  @Test
  void testPollByCidNotFound() {
    UUID cid1 = UUID.randomUUID();
    UUID cid2 = UUID.randomUUID();

    ServiceMessage message1 = new ServiceMessage().cid(cid1).qualifier("test1");
    ServiceMessage message2 = new ServiceMessage().cid(cid2).qualifier("test2");

    ringBuffer.offer(message1);
    ringBuffer.offer(message2);

    UUID nonexistentCid = UUID.randomUUID();
    ServiceMessage result = receiver.poll(msg -> msg.cid().equals(nonexistentCid));

    assertNull(result, "Non-existing cid");
  }

  private ServiceMessage createServiceMessage(UUID uuid) {
    return new ServiceMessage().cid(uuid).qualifier(randomAlphanumeric(5)).data("Some data");
  }

  private ServiceMessage createServiceMessage(String qualifier) {
    return new ServiceMessage().cid(UUID.randomUUID()).qualifier(qualifier).data("Some data");
  }
}
