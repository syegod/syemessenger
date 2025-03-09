package io.syemessenger.api.messagehistory;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.messagehistory.MessageHistoryAssertions.insertRecords;
import static io.syemessenger.api.messagehistory.MessageHistoryAssertions.toUTC;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static io.syemessenger.environment.AssertionUtils.getFields;
import static io.syemessenger.environment.AssertionUtils.toComparator;
import static io.syemessenger.environment.IntegrationEnvironment.cleanTables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.OrderBy;
import io.syemessenger.api.OrderBy.Direction;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.message.MessageInfo;
import io.syemessenger.api.room.BlockMembersRequest;
import io.syemessenger.api.room.RemoveMembersRequest;
import io.syemessenger.api.room.RoomInfo;
import io.syemessenger.environment.FromToTimestamp;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import io.syemessenger.environment.OffsetLimit;
import java.sql.SQLException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class ListMessagesIT {

  @AfterEach
  void afterEach(DataSource dataSource) {
    cleanTables(dataSource);
  }

  @Test
  void testListMessagesNotLoggedIn(ClientSdk clientSdk) {
    try {
      clientSdk.messageHistorySdk().listMessages(new ListMessagesRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testListMessagesFailedMethodSource")
  void testListMessagesFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.messageHistorySdk().listMessages(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, ListMessagesRequest request, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testListMessagesFailedMethodSource() {
    return Stream.of(
        new FailedArgs("No room id", new ListMessagesRequest(), 400, "Missing or invalid: roomId"),
        new FailedArgs(
            "Room not exists",
            new ListMessagesRequest().roomId(Long.MAX_VALUE),
            404,
            "Room not found"),
        new FailedArgs(
            "Offset is negative",
            new ListMessagesRequest().roomId(Long.MAX_VALUE).offset(-50),
            400,
            "Missing or invalid: offset"),
        new FailedArgs(
            "Limit is negative",
            new ListMessagesRequest().roomId(Long.MAX_VALUE).limit(-50),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Limit is over than max",
            new ListMessagesRequest().roomId(Long.MAX_VALUE).limit(60),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Keyword is over than max",
            new ListMessagesRequest().roomId(Long.MAX_VALUE).keyword(randomAlphanumeric(65)),
            400,
            "Missing or invalid: keyword"),
        new FailedArgs(
            "Keyword is less than min",
            new ListMessagesRequest().roomId(Long.MAX_VALUE).keyword(randomAlphanumeric(2)),
            400,
            "Missing or invalid: keyword"),
        new FailedArgs(
            "From is 10 days ahead",
            new ListMessagesRequest().roomId(Long.MAX_VALUE).from(LocalDateTime.now().plusDays(10)),
            400,
            "Missing or invalid: from"),
        new FailedArgs(
            "From is ahead of to",
            new ListMessagesRequest()
                .roomId(Long.MAX_VALUE)
                .from(LocalDateTime.now().minusDays(5))
                .to(LocalDateTime.now().minusDays(6)),
            400,
            "Missing or invalid: 'from' later than 'to'"),
        new FailedArgs(
            "No timezone provided",
            new ListMessagesRequest()
                .roomId(Long.MAX_VALUE)
                .from(LocalDateTime.now().minusDays(10))
                .to(LocalDateTime.now().minusDays(5)),
            400,
            "Missing or invalid: timezone"));
  }

  @Test
  void testListMessagesNotMember(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    try {
      clientSdk.messageHistorySdk().listMessages(new ListMessagesRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not a room member");
    }
  }

  @Test
  void testListMessagesLeaved(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    clientSdk.roomSdk().joinRoom(roomInfo.name());
    clientSdk.roomSdk().leaveRoom(roomInfo.id());
    try {
      clientSdk.messageHistorySdk().listMessages(new ListMessagesRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not a room member");
    }
  }

  @Test
  void testListMessagesBlocked(
      ClientSdk clientSdk,
      ClientSdk anotherClientSdk,
      AccountInfo accountInfo,
      AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    login(anotherClientSdk, anotherAccountInfo);

    anotherClientSdk.roomSdk().joinRoom(roomInfo.name());
    clientSdk
        .roomSdk()
        .blockRoomMembers(
            new BlockMembersRequest().roomId(roomInfo.id()).memberIds(anotherAccountInfo.id()));

    try {
      anotherClientSdk
          .messageHistorySdk()
          .listMessages(new ListMessagesRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not a room member");
    }
  }

  @Test
  void testListMessagesRemoved(
      ClientSdk clientSdk,
      ClientSdk anotherClientSdk,
      AccountInfo accountInfo,
      AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    login(anotherClientSdk, anotherAccountInfo);

    anotherClientSdk.roomSdk().joinRoom(roomInfo.name());
    clientSdk
        .roomSdk()
        .removeRoomMembers(
            new RemoveMembersRequest().roomId(roomInfo.id()).memberIds(anotherAccountInfo.id()));
    try {
      anotherClientSdk
          .messageHistorySdk()
          .listMessages(new ListMessagesRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not a room member");
    }
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "{0}")
  @MethodSource("testListMessagesMethodSource")
  void testListMessages(
      SuccessArgs args, ClientSdk clientSdk, AccountInfo accountInfo, DataSource dataSource)
      throws SQLException {
    final var roomInfo = createRoom(accountInfo);
    final long n = 25;

    final var request = args.request.apply(roomInfo);
    final var keyword = request.keyword();
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;
    final var from = request.from();
    final var to = request.to();
    String timezone = request.timezone();

    login(clientSdk, accountInfo);

    List<MessageRecord> messageRecords = new ArrayList<>();
    for (long i = 1; i <= n; i++) {
      final var message = "test@" + i;
      final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
      final var newMessageRecord =
          new MessageRecord(i, accountInfo.id(), roomInfo.id(), message, now.minusDays(n - i));
      messageRecords.add(newMessageRecord);
    }

    insertRecords(dataSource, messageRecords);

    messageRecords =
        messageRecords.stream()
            .filter(
                messageRecord -> {
                  if (keyword != null) {
                    return messageRecord.message().contains(keyword);
                  } else {
                    return true;
                  }
                })
            .filter(
                messageRecord -> {
                  if (from != null && to != null) {
                    return messageRecord.timestamp().isAfter(toUTC(from, timezone))
                        && messageRecord.timestamp().isBefore(toUTC(to, timezone));
                  } else if (from != null) {
                    return messageRecord.timestamp().isAfter(toUTC(from, timezone));
                  } else if (to != null) {
                    return messageRecord.timestamp().isBefore(toUTC(to, timezone));
                  } else {
                    return true;
                  }
                })
            .toList();

    final var expectedMessageRecords =
        messageRecords.stream().sorted(args.comparator).skip(offset).limit(limit).toList();

    final var response = clientSdk.messageHistorySdk().listMessages(request);
    assertEquals(messageRecords.size(), response.totalCount(), "totalCount");

    final var actualRecords =
        response.messages().stream().map(MessageHistoryAssertions::toMessageRecord).toList();

    assertCollections(
        expectedMessageRecords, actualRecords, MessageHistoryAssertions::assertMessageRecord);
  }

  @SuppressWarnings("rawtypes")
  private record SuccessArgs(
      String test, Function<RoomInfo, ListMessagesRequest> request, Comparator comparator) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testListMessagesMethodSource() {
    final var builder = Stream.<SuccessArgs>builder();

    final String[] fields = getFields(MessageInfo.class);
    final Direction[] directions = {Direction.ASC, Direction.DESC, null};

    // Sort by fields

    for (String field : fields) {
      for (Direction direction : directions) {
        final var orderBy = new OrderBy().field(field).direction(direction);
        builder.add(
            new SuccessArgs(
                "Field: " + field + ", direction: " + direction,
                roomInfo -> new ListMessagesRequest().roomId(roomInfo.id()).orderBy(orderBy),
                toComparator(orderBy)));
      }
    }

    // Filter by keyword

    final String[] keywords = {"test@1", "tes", null};
    for (String keyword : keywords) {
      builder.add(
          new SuccessArgs(
              "Keyword: " + keyword,
              roomInfo -> new ListMessagesRequest().roomId(roomInfo.id()).keyword(keyword),
              Comparator.comparing(MessageRecord::id)));
    }

    // Pagination

    final OffsetLimit[] offsetLimits = {
      new OffsetLimit(null, null),
      new OffsetLimit(null, 5),
      new OffsetLimit(10, null),
      new OffsetLimit(5, 10),
      new OffsetLimit(10, 5),
      new OffsetLimit(20, 10)
    };

    for (OffsetLimit offsetLimit : offsetLimits) {
      final var offset = offsetLimit.offset();
      final var limit = offsetLimit.limit();
      builder.add(
          new SuccessArgs(
              "Offset: " + offset + ", limit: " + limit,
              roomInfo ->
                  new ListMessagesRequest().roomId(roomInfo.id()).offset(offset).limit(limit),
              Comparator.comparing(MessageRecord::id)));
    }

    // Filter by date

    final var timezone = "America/Los_Angeles";
    final var now = LocalDateTime.now(Clock.systemUTC());
    final FromToTimestamp[] fromToTimestampArray = {
      new FromToTimestamp(now.minusDays(10), null, timezone),
      new FromToTimestamp(null, now.minusDays(5), timezone),
      new FromToTimestamp(
          now.minusDays(10), now.minusDays(5), timezone),
      new FromToTimestamp(null, null, timezone),
    };

    for (var fromTo : fromToTimestampArray) {
      final var from = fromTo.from();
      final var to = fromTo.to();
      final var tz = fromTo.timezone();
      builder.add(
          new SuccessArgs(
              "From: " + from + ", to: " + to,
              roomInfo ->
                  new ListMessagesRequest().roomId(roomInfo.id()).from(from).to(to).timezone(tz),
              Comparator.comparing(MessageRecord::id)));
    }

    return builder.build();
  }

  @Test
  @Disabled("https://github.com/syegod/syemessenger/issues/47")
  void testListMessagesWithDifferentTimezones(
      ClientSdk clientSdk, AccountInfo accountInfo, DataSource dataSource) throws SQLException {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    String timezone = "America/Los_Angeles";
    LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
    insertRecords(
        dataSource,
        List.of(
            new MessageRecord(1L, accountInfo.id(), roomInfo.id(), "test123", now.minusHours(2))));

    final var response =
        clientSdk
            .messageHistorySdk()
            .listMessages(
                new ListMessagesRequest()
                    .roomId(roomInfo.id())
                    .from(now.minusHours(3))
                    .timezone(timezone));

    assertEquals(1, response.totalCount());
  }
}
