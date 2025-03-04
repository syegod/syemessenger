package io.syemessenger.api.messagehistory;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.messagehistory.MessageHistoryAssertions.createMessageInfo;
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
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import io.syemessenger.environment.OffsetLimit;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class ListMessagesIT {

  @BeforeEach
  void beforeEach(DataSource dataSource) {
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
            "Missing or invalid: 'to' should be ahead of 'from'"),
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
  void testListMessages(SuccessArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    final int n = 25;

    final var request = args.request.apply(roomInfo);
    final var keyword = request.keyword();
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;

    login(clientSdk, accountInfo);
    clientSdk.messageSdk().subscribe(roomInfo.id());

    List<MessageInfo> messageInfos = new ArrayList<>();

    for (int i = 1; i <= n; i++) {
      final var message = "test@" + i;
      clientSdk.messageSdk().send(message);
      final var now = LocalDateTime.now(Clock.systemUTC()).truncatedTo(ChronoUnit.MILLIS);
      final var newMessageInfo = createMessageInfo(message, accountInfo.id(), roomInfo.id(), now);
      messageInfos.add(newMessageInfo);
    }

    final var expectedMessageInfos =
        messageInfos.stream()
            .filter(
                messageInfo -> {
                  if (keyword != null) {
                    return messageInfo.message().contains(keyword);
                  } else {
                    return true;
                  }
                })
            .sorted(args.comparator)
            .skip(offset)
            .limit(limit)
            .toList();

    try {
      Thread.sleep(500);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    final var response = clientSdk.messageHistorySdk().listMessages(request);
    assertEquals(messageInfos.size(), response.totalCount(), "totalCount");
    assertCollections(
        expectedMessageInfos, response.messages(), MessageHistoryAssertions::assertMessage);
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
              Comparator.<MessageInfo, Long>comparing(MessageInfo::id)));
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
              Comparator.<MessageInfo, Long>comparing(MessageInfo::id)));
    }

    return builder.build();
  }

  @Test
  void testListMessagesWithTimeSpan() {
    fail("Implement");
  }
}
