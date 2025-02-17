package io.syemessenger.kafka;

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
import java.nio.ByteBuffer;
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

  public static ByteBuffer encodeLeaveRoomEvent(LeaveRoomEvent leaveRoomEvent) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    headerEncoder
        .wrap(directBuffer, 0)
        .blockLength(leaveRoomEncoder.sbeBlockLength())
        .templateId(leaveRoomEncoder.sbeTemplateId())
        .schemaId(leaveRoomEncoder.sbeSchemaId())
        .version(leaveRoomEncoder.sbeSchemaVersion());

    leaveRoomEncoder
        .wrap(directBuffer, headerEncoder.encodedLength())
        .roomId(leaveRoomEvent.roomId())
        .accountId(leaveRoomEvent.accountId())
        .isOwner(leaveRoomEvent.isOwner() ? BooleanType.TRUE : BooleanType.FALSE);

    byteBuffer.limit(leaveRoomEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static LeaveRoomEvent decodeLeaveRoomEvent(ByteBuffer byteBuffer) {
    final var directBuffer = new UnsafeBuffer(byteBuffer);
    headerDecoder.wrap(directBuffer, 0);

    leaveRoomDecoder.wrap(
        directBuffer,
        headerDecoder.encodedLength(),
        headerDecoder.blockLength(),
        headerDecoder.version());

    return new LeaveRoomEvent()
        .roomId(leaveRoomDecoder.roomId())
        .accountId(leaveRoomDecoder.accountId())
        .isOwner(leaveRoomDecoder.isOwner() == BooleanType.TRUE);
  }

  public static ByteBuffer encodeRemoveMembersEvent(RemoveMembersEvent removeMembersEvent) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    headerEncoder
        .wrap(directBuffer, 0)
        .blockLength(removeMembersEncoder.sbeBlockLength())
        .templateId(removeMembersEncoder.sbeTemplateId())
        .schemaId(removeMembersEncoder.sbeSchemaId())
        .version(removeMembersEncoder.sbeSchemaVersion());

    removeMembersEncoder
        .wrap(directBuffer, headerEncoder.encodedLength())
        .roomId(removeMembersEvent.roomId());

    RemoveMembersEventEncoder.MemberIdsEncoder memberIdsEncoder =
        removeMembersEncoder.memberIdsCount(removeMembersEvent.memberIds().size());

    for (var i : removeMembersEvent.memberIds()) {
      memberIdsEncoder.memberId(i);
    }
    byteBuffer.limit(removeMembersEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static RemoveMembersEvent decodeRemoveMembersEvent(ByteBuffer byteBuffer) {
    final var directBuffer = new UnsafeBuffer(byteBuffer);
    headerDecoder.wrap(directBuffer, 0);

    removeMembersDecoder.wrap(
        directBuffer,
        headerDecoder.encodedLength(),
        headerDecoder.blockLength(),
        headerDecoder.version());

    final var memberIds = new ArrayList<Long>();
    for (var memberId : removeMembersDecoder.memberIds()) {
      memberIds.add(memberId.memberId());
    }

    return new RemoveMembersEvent().roomId(removeMembersDecoder.roomId()).memberIds(memberIds);
  }

  public static ByteBuffer encodeBlockMembersEvent(BlockMembersEvent blockMembersEvent) {
    final var byteBuffer = ByteBuffer.allocate(512);
    final var directBuffer = new UnsafeBuffer(byteBuffer);

    headerEncoder
        .wrap(directBuffer, 0)
        .blockLength(blockMembersEncoder.sbeBlockLength())
        .templateId(blockMembersEncoder.sbeTemplateId())
        .schemaId(blockMembersEncoder.sbeSchemaId())
        .version(blockMembersEncoder.sbeSchemaVersion());

    blockMembersEncoder
        .wrap(directBuffer, headerEncoder.encodedLength())
        .roomId(blockMembersEvent.roomId());

    var memberIdsEncoder = blockMembersEncoder.memberIdsCount(blockMembersEvent.memberIds().size());

    for (var i : blockMembersEvent.memberIds()) {
      memberIdsEncoder.memberId(i);
    }
    byteBuffer.limit(blockMembersEncoder.encodedLength() + headerEncoder.encodedLength());
    return byteBuffer;
  }

  public static BlockMembersEvent decodeBlockMembersEvent(ByteBuffer byteBuffer) {
    final var directBuffer = new UnsafeBuffer(byteBuffer);
    headerDecoder.wrap(directBuffer, 0);

    blockMembersDecoder.wrap(
        directBuffer,
        headerDecoder.encodedLength(),
        headerDecoder.blockLength(),
        headerDecoder.version());

    final var memberIds = new ArrayList<Long>();
    for (var memberId : blockMembersDecoder.memberIds()) {
      memberIds.add(memberId.memberId());
    }

    return new BlockMembersEvent().roomId(blockMembersDecoder.roomId()).memberIds(memberIds);
  }
}
