package io.syemessenger.api.messagehistory;

import static io.syemessenger.api.Pageables.toPageable;

import io.syemessenger.LocalDateTimeConverter;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.api.messagehistory.repository.HistoryMessage;
import io.syemessenger.api.messagehistory.repository.HistoryMessageRepository;
import io.syemessenger.api.room.repository.RoomRepository;
import io.syemessenger.websocket.SessionContext;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageHistoryService {

  private final RoomRepository roomRepository;
  private final HistoryMessageRepository historyMessageRepository;

  public MessageHistoryService(
      RoomRepository roomRepository, HistoryMessageRepository historyMessageRepository) {
    this.roomRepository = roomRepository;
    this.historyMessageRepository = historyMessageRepository;
  }

  @Transactional
  public void saveMessage(MessageInfo messageInfo) {
    final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
    try {
      historyMessageRepository.save(
          new HistoryMessage()
              .roomId(messageInfo.roomId())
              .senderId(messageInfo.senderId())
              .message(messageInfo.message())
              .timestamp(now));
    } catch (Exception ex) {
      throw new RuntimeException(ex.getMessage());
    }
  }

  public Page<HistoryMessage> listMessages(
      SessionContext sessionContext, ListMessagesRequest request) {
    final var room = roomRepository.findById(request.roomId()).orElse(null);
    if (room == null) {
      throw new ServiceException(404, "Room not found");
    }

    final var roomMember =
        roomRepository.findRoomMember(request.roomId(), sessionContext.accountId());
    if (roomMember == null) {
      throw new ServiceException(403, "Not a room member");
    }

    final var pageable = toPageable(request.offset(), request.limit(), request.orderBy());

    var keyword = request.keyword();
    if (keyword == null) {
      keyword = "";
    }

    final var localDateTimeConverter = new LocalDateTimeConverter();

    Timestamp fromTimestamp;
    final var timezone = request.timezone();
    if (request.from() == null) {
      fromTimestamp = Timestamp.from(Instant.EPOCH);
    } else {
      fromTimestamp =
          localDateTimeConverter.convertToDatabaseColumn(toUTC(request.from(), timezone));
    }

    Timestamp toTimestamp;
    if (request.to() == null) {
      toTimestamp = Timestamp.valueOf(LocalDateTime.now());
    } else {
      toTimestamp = localDateTimeConverter.convertToDatabaseColumn(toUTC(request.to(), timezone));
    }

    return historyMessageRepository.findByKeywordAndTimestamp(
        keyword, fromTimestamp, toTimestamp, pageable);
  }

  private static LocalDateTime toUTC(LocalDateTime localDateTime, String timezone) {
    final var zoneId = ZoneId.of(timezone);
    final var zonedDateTime = localDateTime.atZone(zoneId);
    return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
  }
}
