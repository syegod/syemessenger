package io.syemessenger.outboxprocessor;


import io.syemessenger.outboxprocessor.repository.OutboxRoomEvent;
import io.syemessenger.outboxprocessor.repository.RoomEventRepository;
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
public class OutboxProcessor implements AutoCloseable {

  private static final Logger LOGGER = LoggerFactory.getLogger(OutboxProcessor.class);

  private final RoomEventRepository roomEventRepository;
  private final KafkaTemplate<Long, ByteBuffer> kafkaTemplate;

  private ExecutorService executorService;
  private boolean isStopped;

  public OutboxProcessor(
      RoomEventRepository roomEventRepository, KafkaTemplate<Long, ByteBuffer> kafkaTemplate) {
    this.roomEventRepository = roomEventRepository;
    this.kafkaTemplate = kafkaTemplate;
  }

  @PostConstruct
  public void init() {
    executorService = Executors.newSingleThreadExecutor();
    executorService.execute(() -> doWork());
  }

  private void doWork() {
    while (true) {
      if (isStopped) {
        break;
      }
      try {
        run();
        Thread.sleep(100);
      } catch (Exception ex) {
        LOGGER.error("[doWork] Exception occurred", ex);
        try {
          Thread.sleep(3000);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  private void run() {
    Long position = roomEventRepository.getPosition();
    if (position == null) {
      position = 0L;
    }
    var events = roomEventRepository.findOutboxRoomEvents(position);
    for (var event : events) {
      onEvent(event);
    }
  }

  private void onEvent(OutboxRoomEvent event) {
    try {
      kafkaTemplate
          .send("messages", event.roomId(), ByteBuffer.wrap(event.data()))
          .get(3, TimeUnit.SECONDS);
      roomEventRepository.saveRoomEventPosition(event.id());
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
