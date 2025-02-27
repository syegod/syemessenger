package io.syemessenger.api.room.outbox;

import io.syemessenger.IdleStrategy;
import io.syemessenger.ServiceConfig;
import io.syemessenger.api.room.outbox.repository.OutboxRoomEvent;
import io.syemessenger.api.room.outbox.repository.RoomEventRepository;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;

@Named
public class OutboxRoomProcessor implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxRoomProcessor.class);

  private final ServiceConfig config;
  private final IdleStrategy sleepStrategy;
  private final RoomEventRepository roomEventRepository;
  private final KafkaTemplate<Long, ByteBuffer> kafkaTemplate;

  private ExecutorService executorService;
  private boolean isStopped;

  public OutboxRoomProcessor(
      ServiceConfig config,
      IdleStrategy sleepStrategy,
      RoomEventRepository roomEventRepository,
      KafkaTemplate<Long, ByteBuffer> kafkaTemplate) {
    this.config = config;
    this.sleepStrategy = sleepStrategy;
    this.roomEventRepository = roomEventRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  @PostConstruct
  public void init() {
    executorService = Executors.newSingleThreadExecutor();
    executorService.execute(this::run);
  }

  void doWork() {
    long delay;
    try {
      handleEvents();
      delay = config.roomOutboxProcessorRunDelay();
    } catch (Exception ex) {
      LOGGER.error("[doWork] Exception occurred", ex);
      delay = config.roomOutboxProcessorRunDelay() * 10L;
    }
    sleepStrategy.idle(delay);
  }

  private void run() {
    while (!isStopped) {
      doWork();
    }
  }

  private void handleEvents() {
    Long position = roomEventRepository.getPosition();
    if (position == null) {
      position = 0L;
    }
    final var events = roomEventRepository.listEvents(position);
    for (var event : events) {
      onEvent(event);
    }
  }

  private void onEvent(OutboxRoomEvent event) {
    try {
      kafkaTemplate
          .send("messages", event.roomId(), ByteBuffer.wrap(event.data()))
          .get(3, TimeUnit.SECONDS);
      roomEventRepository.savePosition(event.id());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void close() {
    isStopped = true;
    if (executorService != null) {
      executorService.close();
    }
  }
}
