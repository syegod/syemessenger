package io.syemessenger.kafka;

import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static io.syemessenger.kafka.KafkaMessageCodec.*;
import static org.junit.jupiter.api.Assertions.*;

import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.kafka.dto.BlockMembersEvent;
import io.syemessenger.kafka.dto.LeaveRoomEvent;
import io.syemessenger.kafka.dto.RemoveMembersEvent;
import java.util.List;
import org.junit.jupiter.api.Test;

class KafkaMessageCodecTest {

  @Test
  void testRoomMessage() {
    long roomId = Long.MAX_VALUE;
    long senderId = Long.MAX_VALUE;
    String message = "Test message";
    final var messageInfo = new MessageInfo().roomId(senderId).senderId(roomId).message(message);

    final var buffer = encodeRoomMessage(messageInfo);
    final var decodedMessageInfo = decodeRoomMessage(buffer);

    assertEquals(messageInfo.roomId(), decodedMessageInfo.roomId());
    assertEquals(messageInfo.senderId(), decodedMessageInfo.senderId());
    assertEquals(messageInfo.message(), decodedMessageInfo.message());
  }

  @Test
  void testLeaveRoomEvent() {
    long accountId = Long.MAX_VALUE;
    long roomId = Long.MAX_VALUE;
    boolean isOwner = false;
    final var leaveRoomEvent =
        new LeaveRoomEvent().isOwner(isOwner).roomId(roomId).accountId(accountId);

    final var buffer = encodeLeaveRoomEvent(leaveRoomEvent);
    final var decodedLeaveRoomEvent = decodeLeaveRoomEvent(buffer);

    assertEquals(leaveRoomEvent.roomId(), decodedLeaveRoomEvent.roomId());
    assertEquals(leaveRoomEvent.accountId(), decodedLeaveRoomEvent.accountId());
    assertEquals(leaveRoomEvent.isOwner(), decodedLeaveRoomEvent.isOwner());
  }

  @Test
  void testRemoveRoomMembersEvent() {
    final var roomId = Long.MAX_VALUE;
    final var memberIds = List.of(100000L);
    final var removeMembersEvent = new RemoveMembersEvent().roomId(roomId).memberIds(memberIds);

    final var buffer = encodeRemoveMembersEvent(removeMembersEvent);
    final var decodedRemoveMembersEvent = decodeRemoveMembersEvent(buffer);

    assertEquals(removeMembersEvent.roomId(), decodedRemoveMembersEvent.roomId());
    assertCollections(
        removeMembersEvent.memberIds(), decodedRemoveMembersEvent.memberIds(), Long::equals);
  }

  @Test
  void testBlockRoomMembersEvent() {
    final var roomId = Long.MAX_VALUE;
    final var memberIds = List.of(100000L);
    final var blockMembersEvent = new BlockMembersEvent().roomId(roomId).memberIds(memberIds);

    final var buffer = encodeBlockMembersEvent(blockMembersEvent);
    final var decodedBlockMembersEvent = decodeBlockMembersEvent(buffer);

    assertEquals(blockMembersEvent.roomId(), decodedBlockMembersEvent.roomId());
    assertCollections(
        blockMembersEvent.memberIds(), decodedBlockMembersEvent.memberIds(), Long::equals);
  }
}
