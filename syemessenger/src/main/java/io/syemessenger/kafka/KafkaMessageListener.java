package io.syemessenger.kafka;

import java.nio.ByteBuffer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {

  @KafkaListener(topics = "leave-room", groupId = "test-group")
  public void listen(ByteBuffer message) {
    final var leaveRoomEvent = KafkaMessageCodec.decodeLeaveRoomEvent(message);
    System.out.println("Received message: " + leaveRoomEvent);
  }
}
