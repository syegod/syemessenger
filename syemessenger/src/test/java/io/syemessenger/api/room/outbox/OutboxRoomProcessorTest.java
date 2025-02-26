package io.syemessenger.api.room.outbox;

import io.syemessenger.ServiceConfig;
import io.syemessenger.IdleStrategy;
import io.syemessenger.api.room.outbox.repository.RoomEventRepository;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OutboxRoomProcessorTest {

  @Mock private ServiceConfig config;
  @Mock private IdleStrategy sleepStrategy;
  @Mock private RoomEventRepository roomEventRepository;
  @Mock private KafkaTemplate<Long, ByteBuffer> kafkaTemplate;

  private OutboxRoomProcessor outboxRoomProcessor;

  @BeforeEach
  void beforeEach() {
    outboxRoomProcessor =
        new OutboxRoomProcessor(config, sleepStrategy, roomEventRepository, kafkaTemplate);
  }

  @Test
  void test1() {}

  @Test
  void test2() {}

  @Test
  void test3() {}
}
