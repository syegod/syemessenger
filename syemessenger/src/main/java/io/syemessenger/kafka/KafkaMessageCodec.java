package io.syemessenger.kafka;

import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.kafka.dto.BlockMembersEvent;
import io.syemessenger.kafka.dto.LeaveRoomEvent;
import io.syemessenger.kafka.dto.RemoveMembersEvent;
import io.syemessenger.sbe.BlockMembersEventDecoder;
import io.syemessenger.sbe.BlockMembersEventEncoder;
import io.syemessenger.sbe.BooleanType;
import io.syemessenger.sbe.LeaveRoomEventDecoder;
import io.syemessenger.sbe.LeaveRoomEventEncoder;
import io.syemessenger.sbe.MessageHeaderDecoder;
import io.syemessenger.sbe.MessageHeaderEncoder;
import io.syemessenger.sbe.RemoveMembersEventDecoder;
import io.syemessenger.sbe.RemoveMembersEventEncoder;
import io.syemessenger.sbe.RoomMessageDecoder;
import io.syemessenger.sbe.RoomMessageEncoder;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import org.agrona.concurrent.UnsafeBuffer;

public class KafkaMessageCodec {

  private static final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
  private static final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
  private static final LeaveRoomEventEncoder leaveRoomEncoder = new LeaveRoomEventEncoder();
  private static final LeaveRoomEventDecoder leaveRoomDecoder = new LeaveRoomEventDecoder();
  private static final RemoveMembersEventEncoder removeMembersEncoder =
      new RemoveMembersEventEncoder();
  private static final RemoveMembersEventDecoder removeMembersDecoder =
      new RemoveMembersEventDecoder();
  private static final BlockMembersEventEncoder blockMembersEncoder =
      new BlockMembersEventEncoder();
  private static final BlockMembersEventDecoder blockMembersDecoder =
      new BlockMembersEventDecoder();
  private static final RoomMessageEncoder roomMessageEncoder = new RoomMessageEncoder();
  private static final RoomMessageDecoder roomMessageDecoder = new RoomMessageDecoder();

  public static ByteBuffer encodeLeaveRoomEvent(LeaveRoomEvent leaveRoomEvent) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    leaveRoomEncoder
        .wrapAndApplyHeader(directBuffer, 0, headerEncoder)
        .roomId(leaveRoomEvent.roomId())
        .accountId(leaveRoomEvent.accountId())
        .isOwner(leaveRoomEvent.isOwner() ? BooleanType.TRUE : BooleanType.FALSE);

    byteBuffer.limit(leaveRoomEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static LeaveRoomEvent decodeLeaveRoomEvent(ByteBuffer byteBuffer) {
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    leaveRoomDecoder.wrapAndApplyHeader(directBuffer, 0, headerDecoder);

    return new LeaveRoomEvent()
        .roomId(leaveRoomDecoder.roomId())
        .accountId(leaveRoomDecoder.accountId())
        .isOwner(leaveRoomDecoder.isOwner() == BooleanType.TRUE);
  }

  public static ByteBuffer encodeRemoveMembersEvent(RemoveMembersEvent removeMembersEvent) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    removeMembersEncoder
        .wrapAndApplyHeader(directBuffer, 0, headerEncoder)
        .roomId(removeMembersEvent.roomId());

    final var memberIdsEncoder =
        removeMembersEncoder.memberIdsCount(removeMembersEvent.memberIds().size());

    for (var i : removeMembersEvent.memberIds()) {
      memberIdsEncoder.next().memberId(i);
    }
    byteBuffer.limit(removeMembersEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static RemoveMembersEvent decodeRemoveMembersEvent(ByteBuffer byteBuffer) {
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    removeMembersDecoder.wrapAndApplyHeader(directBuffer, 0, headerDecoder);

    final var memberIds = new ArrayList<Long>();
    for (var memberId : removeMembersDecoder.memberIds()) {
      memberIds.add(memberId.memberId());
    }

    return new RemoveMembersEvent().roomId(removeMembersDecoder.roomId()).memberIds(memberIds);
  }

  public static ByteBuffer encodeBlockMembersEvent(BlockMembersEvent blockMembersEvent) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    blockMembersEncoder
        .wrapAndApplyHeader(directBuffer, 0, headerEncoder)
        .roomId(blockMembersEvent.roomId());

    var memberIdsEncoder = blockMembersEncoder.memberIdsCount(blockMembersEvent.memberIds().size());

    for (var i : blockMembersEvent.memberIds()) {
      memberIdsEncoder.next().memberId(i);
    }
    byteBuffer.limit(blockMembersEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static BlockMembersEvent decodeBlockMembersEvent(ByteBuffer byteBuffer) {
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    blockMembersDecoder.wrapAndApplyHeader(directBuffer, 0, headerDecoder);

    final var memberIds = new ArrayList<Long>();
    for (var memberId : blockMembersDecoder.memberIds()) {
      memberIds.add(memberId.memberId());
    }

    return new BlockMembersEvent().roomId(blockMembersDecoder.roomId()).memberIds(memberIds);
  }

  public static ByteBuffer encodeRoomMessage(MessageInfo messageInfo) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    roomMessageEncoder
        .wrapAndApplyHeader(directBuffer, 0, headerEncoder)
        .roomId(messageInfo.roomId())
        .message(messageInfo.message())
        .senderId(messageInfo.senderId())
        .timestamp(messageInfo.timestamp().toInstant(ZoneOffset.UTC).toEpochMilli());
    byteBuffer.limit(roomMessageEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static MessageInfo decodeRoomMessage(ByteBuffer byteBuffer) {
    final var unsafeBuffer = new UnsafeBuffer(byteBuffer);
    roomMessageDecoder.wrapAndApplyHeader(unsafeBuffer, 0, headerDecoder);

    return new MessageInfo()
        .message(roomMessageDecoder.message())
        .roomId(roomMessageDecoder.roomId())
        .senderId(roomMessageDecoder.senderId())
        .timestamp(
            LocalDateTime.ofInstant(
                Instant.ofEpochMilli(roomMessageDecoder.timestamp()), ZoneOffset.UTC));
  }
}
