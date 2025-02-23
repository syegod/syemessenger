package io.syemessenger.kafka;

import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.kafka.dto.BlockMembersEvent;
import io.syemessenger.kafka.dto.LeaveRoomEvent;
import io.syemessenger.kafka.dto.RemoveMembersEvent;
import io.syemessenger.sbe.BlockMembersEventDecoder;
import io.syemessenger.sbe.LeaveRoomEventDecoder;
import io.syemessenger.sbe.MessageHeaderDecoder;
import io.syemessenger.sbe.RemoveMembersEventDecoder;
import io.syemessenger.sbe.RoomMessageDecoder;
import java.nio.ByteBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaMessageListener {

  @KafkaListener(topics = "messages")
  public void listenMessages(ByteBuffer byteBuffer) {
    final var headerDecoder = new MessageHeaderDecoder();
    final var directBuffer = new UnsafeBuffer(byteBuffer);
    headerDecoder.wrap(directBuffer, 0);

    if (headerDecoder.schemaId() != MessageHeaderDecoder.SCHEMA_ID) {
      throw new IllegalArgumentException("Wrong schemaId: " + headerDecoder.schemaId());
    }

    final var templateId = headerDecoder.templateId();

    switch (templateId) {
      case LeaveRoomEventDecoder.TEMPLATE_ID:
        onLeaveRoomEvent(KafkaMessageCodec.decodeLeaveRoomEvent(byteBuffer));
        break;
      case RemoveMembersEventDecoder.TEMPLATE_ID:
        onRemoveMembersEvent(KafkaMessageCodec.decodeRemoveMembersEvent(byteBuffer));
        break;
      case BlockMembersEventDecoder.TEMPLATE_ID:
        onBlockMembersEvent(KafkaMessageCodec.decodeBlockMembersEvent(byteBuffer));
        break;
      case RoomMessageDecoder.TEMPLATE_ID:
        onRoomMessage(KafkaMessageCodec.decodeRoomMessage(byteBuffer));
        break;
      default:
        throw new IllegalArgumentException("Wrong templateId: " + headerDecoder.templateId());
    }
  }

  private void onLeaveRoomEvent(LeaveRoomEvent leaveRoomEvent) {
    // TODO: implement
  }

  private void onRemoveMembersEvent(RemoveMembersEvent removeMembersEvent) {
    // TODO: implement
  }

  private void onBlockMembersEvent(BlockMembersEvent blockMembersEvent) {
    // TODO: implement
  }

  private void onRoomMessage(MessageInfo messageInfo) {
    // TODO: implement
  }
}
