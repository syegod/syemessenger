package io.syemessenger.api.messagehistory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.syemessenger.LocalDateTimeConverter;
import io.syemessenger.api.message.MessageInfo;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import javax.sql.DataSource;

public class MessageHistoryAssertions {

  public static void assertMessageRecord(MessageRecord expected, MessageRecord actual) {
    assertEquals(expected.id(), actual.id(), "actual.id: " + actual.id());
    assertEquals(expected.message(), actual.message(), "actual.message: " + actual.message());
    assertEquals(expected.senderId(), actual.senderId(), "actual.senderId: " + actual.senderId());
    assertEquals(expected.roomId(), actual.roomId(), "actual.roomId: " + actual.roomId());
    assertNotNull(actual.timestamp());
  }

  public static MessageRecord toMessageRecord(MessageInfo messageInfo) {
    return new MessageRecord(
        messageInfo.id(),
        messageInfo.senderId(),
        messageInfo.roomId(),
        messageInfo.message(),
        messageInfo.timestamp());
  }

  public static void insertRecords(
      DataSource dataSource, List<MessageRecord> messageRecords, String timezone)
      throws SQLException {
    try (final var connection = dataSource.getConnection()) {
      connection.setAutoCommit(false);
      for (var record : messageRecords) {
        String query =
            "INSERT INTO messages (sender_id, room_id, message, timestamp) VALUES (?, ?, ?, ?)";
        try (final var preparedStatement = connection.prepareStatement(query)) {
          preparedStatement.setLong(1, record.senderId());
          preparedStatement.setLong(2, record.roomId());
          preparedStatement.setString(3, record.message());
          preparedStatement.setTimestamp(4, toUTCTimestamp(record.timestamp(), timezone));
          preparedStatement.executeUpdate();
          connection.commit();
        }
      }
    }
  }

  private static Timestamp toUTCTimestamp(LocalDateTime localDateTime, String timezone) {
    if (timezone == null) {
      timezone = "UTC";
    }
    final var zoneId = ZoneId.of(timezone);
    final var zonedDateTime = localDateTime.atZone(zoneId);
    LocalDateTime dateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
    return new LocalDateTimeConverter().convertToDatabaseColumn(dateTime);
  }
}
