package io.syemessenger.api.messagehistory;

import static io.syemessenger.environment.CounterUtils.nextLong;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.syemessenger.api.message.MessageInfo;
import java.time.LocalDateTime;

public class MessageHistoryAssertions {

  public static MessageInfo createMessageInfo(
      String message, Long senderId, Long roomId, LocalDateTime timestamp) {
    return new MessageInfo()
        .id(nextLong())
        .message(message)
        .senderId(senderId)
        .roomId(roomId)
        .timestamp(timestamp);
  }

  public static void assertMessage(MessageInfo expected, MessageInfo actual) {
    assertEquals(expected.message(), actual.message(), "actual.message: " + actual.message());
    assertEquals(expected.senderId(), actual.senderId(), "actual.senderId: " + actual.senderId());
    assertEquals(expected.roomId(), actual.roomId(), "actual.roomId: " + actual.roomId());
  }
}
