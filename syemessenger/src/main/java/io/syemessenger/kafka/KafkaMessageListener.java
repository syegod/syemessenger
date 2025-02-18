package io.syemessenger.kafka;

import java.nio.ByteBuffer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {

  @KafkaListener(topics = "messages")
  public void listenMessages(ByteBuffer message) {
    final var leaveRoomEvent = KafkaMessageCodec.decodeLeaveRoomEvent(message);
  }
}
