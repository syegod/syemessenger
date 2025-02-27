package io.syemessenger.api.room.outbox;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.syemessenger.IdleStrategy;
import io.syemessenger.ServiceConfig;
import io.syemessenger.SleepIdleStrategy;
import io.syemessenger.api.room.outbox.repository.OutboxRoomEvent;
import io.syemessenger.api.room.outbox.repository.RoomEventRepository;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    sleepStrategy = mock(SleepIdleStrategy.class);
    outboxRoomProcessor =
        new OutboxRoomProcessor(config, sleepStrategy, roomEventRepository, kafkaTemplate);
    when(config.roomOutboxProcessorRunDelay()).thenReturn(10);
    when(kafkaTemplate.send(anyString(), anyLong(), any(ByteBuffer.class)))
        .thenReturn(CompletableFuture.completedFuture(null));
  }

  @Test
  void testNullPosition() {
    when(roomEventRepository.getPosition()).thenReturn(null);
    when(roomEventRepository.listEvents(0L)).thenReturn(List.of());

    outboxRoomProcessor.doWork();

    verify(roomEventRepository).getPosition();
    verify(roomEventRepository).listEvents(0L);

    verify(kafkaTemplate, never()).send(anyString(), anyLong(), any(ByteBuffer.class));
  }

  @Test
  void testWithEvents() {
    final var position = 5L;
    final var roomId = 100L;
    final var eventId = 6L;
    final var data = new byte[] {1, 2, 3};

    OutboxRoomEvent mockEvent = mock(OutboxRoomEvent.class);
    when(mockEvent.roomId()).thenReturn(roomId);
    when(mockEvent.data()).thenReturn(data);
    when(mockEvent.id()).thenReturn(eventId);

    when(roomEventRepository.getPosition()).thenReturn(position);
    when(roomEventRepository.listEvents(position)).thenReturn(List.of(mockEvent));

    outboxRoomProcessor.doWork();

    verify(roomEventRepository).getPosition();
    verify(roomEventRepository).listEvents(position);

    verify(kafkaTemplate).send(anyString(), eq(roomId), eq(ByteBuffer.wrap(data)));

    verify(roomEventRepository).savePosition(eventId);

    verify(sleepStrategy).idle(eq((long) config.roomOutboxProcessorRunDelay()));
  }

  @Test
  void testKafkaSendThrowsException() {
    final var position = 5L;
    final var roomId = 100L;
    final var eventId = 6L;
    final var data = new byte[] {1, 2, 3};

    OutboxRoomEvent mockEvent = mock(OutboxRoomEvent.class);
    when(mockEvent.roomId()).thenReturn(roomId);
    when(mockEvent.data()).thenReturn(data);
    when(mockEvent.id()).thenReturn(eventId);

    when(roomEventRepository.getPosition()).thenReturn(position);
    when(roomEventRepository.listEvents(position)).thenReturn(List.of(mockEvent));
    when(kafkaTemplate.send(anyString(), anyLong(), any(ByteBuffer.class)))
        .thenThrow(new RuntimeException("Test exception"));

    outboxRoomProcessor.doWork();

    verify(roomEventRepository).getPosition();
    verify(roomEventRepository).listEvents(position);

    verify(roomEventRepository, never()).savePosition(anyLong());

    verify(sleepStrategy).idle(eq(config.roomOutboxProcessorRunDelay() * 10L));
  }

  @Test
  void testGetPositionThrowsException() {
    when(roomEventRepository.getPosition()).thenThrow(new RuntimeException("Test Exception"));

    outboxRoomProcessor.doWork();

    verify(kafkaTemplate, never()).send(anyString(), anyLong(), any(ByteBuffer.class));

    verify(sleepStrategy, atLeastOnce()).idle(config.roomOutboxProcessorRunDelay() * 10L);
  }
}
