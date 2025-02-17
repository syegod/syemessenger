package io.syemessenger.kafka;

import io.syemessenger.kafka.dto.LeaveRoomEvent;
import io.syemessenger.sbe.BooleanType;
import io.syemessenger.sbe.LeaveRoomEventDecoder;
import io.syemessenger.sbe.LeaveRoomEventEncoder;
import io.syemessenger.sbe.MessageHeaderDecoder;
import io.syemessenger.sbe.MessageHeaderEncoder;
import java.nio.ByteBuffer;
import org.agrona.concurrent.UnsafeBuffer;

public class KafkaMessageCodec {

  private static final MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
  private static final MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
  private static final LeaveRoomEventEncoder leaveRoomEncoder = new LeaveRoomEventEncoder();
  private static final LeaveRoomEventDecoder leaveRoomDecoder = new LeaveRoomEventDecoder();

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
}
