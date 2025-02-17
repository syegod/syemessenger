package io.syemessenger.kafka;

import java.nio.ByteBuffer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {

  @KafkaListener(topics = "leave-room")
  public void listenLeaveRoom(ByteBuffer message) {
    final var leaveRoomEvent = KafkaMessageCodec.decodeLeaveRoomEvent(message);
    System.out.println();
  }

  @KafkaListener(topics = "remove-members")
  public void listenRemoveMembers(ByteBuffer message) {
    final var removeMembersEvent = KafkaMessageCodec.decodeRemoveMembersEvent(message);
    System.out.println();
  }

  @KafkaListener(topics = "block-members")
  public void listenBlockMembers(ByteBuffer message) {
    final var blockMembersEvent = KafkaMessageCodec.decodeBlockMembersEvent(message);
    System.out.println();
  }
}
