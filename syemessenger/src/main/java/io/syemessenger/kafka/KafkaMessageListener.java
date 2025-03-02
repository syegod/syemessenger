package io.syemessenger.kafka;

import io.syemessenger.SubscriptionRegistry;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.api.message.MessageService;
import io.syemessenger.api.messagehistory.MessageHistoryService;
import io.syemessenger.api.messagehistory.repository.MessageRepository;
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

  private final SubscriptionRegistry subscriptionRegistry;
  private final MessageHistoryService messageHistoryService;

  public KafkaMessageListener(SubscriptionRegistry subscriptionRegistry,
      MessageHistoryService messageHistoryService) {
    this.subscriptionRegistry = subscriptionRegistry;
    this.messageHistoryService = messageHistoryService;
  }

  @KafkaListener(topics = "messages")
  public void handleMessage(ByteBuffer byteBuffer) {
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

  @KafkaListener(topics = "messages", groupId = "message-history-group")
  public void handleMessageHistory(ByteBuffer byteBuffer) {
    final var headerDecoder = new MessageHeaderDecoder();
    final var directBuffer = new UnsafeBuffer(byteBuffer);
    headerDecoder.wrap(directBuffer, 0);

    if (RoomMessageDecoder.TEMPLATE_ID == headerDecoder.templateId()) {
      final var messageInfo = KafkaMessageCodec.decodeRoomMessage(byteBuffer);
      while (true) {
        try {
          messageHistoryService.saveMessage(messageInfo);
          break;
        } catch (Exception ex) {
          try {
            Thread.sleep(3000);
          } catch (InterruptedException e) {
            throw new RuntimeException(e);
          }
        }
      }
    }
  }

  private void onLeaveRoomEvent(LeaveRoomEvent leaveRoomEvent) {
    subscriptionRegistry.leaveRoom(
        leaveRoomEvent.roomId(), leaveRoomEvent.accountId(), leaveRoomEvent.isOwner());
  }

  private void onRemoveMembersEvent(RemoveMembersEvent removeMembersEvent) {
    subscriptionRegistry.removeMembers(removeMembersEvent.roomId(), removeMembersEvent.memberIds());
  }

  private void onBlockMembersEvent(BlockMembersEvent blockMembersEvent) {
    subscriptionRegistry.blockMembers(blockMembersEvent.roomId(), blockMembersEvent.memberIds());
  }

  private void onRoomMessage(MessageInfo messageInfo) {
    subscriptionRegistry.onRoomMessage(messageInfo);
  }
}
