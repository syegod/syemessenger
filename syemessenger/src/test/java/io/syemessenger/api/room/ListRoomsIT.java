package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static io.syemessenger.environment.AssertionUtils.getFields;
import static io.syemessenger.environment.AssertionUtils.toComparator;
import static io.syemessenger.environment.CounterUtils.nextLong;
import static io.syemessenger.environment.IntegrationEnvironment.cleanTables;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.OrderBy;
import io.syemessenger.api.OrderBy.Direction;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import io.syemessenger.environment.OffsetLimit;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class ListRoomsIT {

  @AfterEach
  void afterEach(DataSource dataSource) {
    cleanTables(dataSource);
  }

  @Test
  void testListRoomsNotLoggedIn(ClientSdk clientSdk) {
    try {
      clientSdk.roomSdk().listRooms(new ListRoomsRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testListRoomsFailedMethodSource")
  void testListRoomsFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, request -> request.username(accountInfo.username()));
    try {
      clientSdk.roomSdk().listRooms(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, ListRoomsRequest request, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testListRoomsFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Offset is negative",
            new ListRoomsRequest().offset(-50),
            400,
            "Missing or invalid: offset"),
        new FailedArgs(
            "Limit is negative",
            new ListRoomsRequest().limit(-50),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Limit is over than max",
            new ListRoomsRequest().limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "{0}")
  @MethodSource("testListRoomsMethodSource")
  void testListRooms(SuccessArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    final int n = 25;
    final var request = args.request;
    final var k = request.keyword();
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;

    login(clientSdk, accountInfo);

    final var roomInfos =
        IntStream.range(0, n)
            .mapToObj(
                v -> {
                  final var l = nextLong();
                  return clientSdk
                      .roomSdk()
                      .createRoom(
                          new CreateRoomRequest()
                              .name("room@" + l)
                              .description("description@" + l));
                })
            .filter(
                roomInfo -> {
                  if (k != null) {
                    return roomInfo.name().contains(k) || roomInfo.description().contains(k);
                  } else {
                    return true;
                  }
                })
            .sorted(args.comparator)
            .toList();

    final var expectedRoomInfos = roomInfos.stream().skip(offset).limit(limit).toList();

    final var response = clientSdk.roomSdk().listRooms(request);
    assertEquals(roomInfos.size(), response.totalCount(), "totalCount");
    assertCollections(expectedRoomInfos, response.roomInfos(), RoomAssertions::assertRoom);
  }

  @SuppressWarnings("rawtypes")
  private record SuccessArgs(String test, ListRoomsRequest request, Comparator comparator) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testListRoomsMethodSource() {
    final var builder = Stream.<SuccessArgs>builder();

    final String[] fields = getFields(RoomInfo.class);
    final Direction[] directions = {Direction.ASC, Direction.DESC, null};

    // Sort by fields

    for (String field : fields) {
      for (Direction direction : directions) {
        final var orderBy = new OrderBy().field(field).direction(direction);
        builder.add(
            new SuccessArgs(
                "Field: " + field + ", direction: " + direction,
                new ListRoomsRequest().orderBy(orderBy),
                toComparator(orderBy)));
      }
    }

    // Filter by keyword

    final String[] keywords = {"room@", "description@", null};
    for (String keyword : keywords) {
      builder.add(
          new SuccessArgs(
              "Keyword: " + keyword,
              new ListRoomsRequest().keyword(keyword),
              Comparator.<RoomInfo, Long>comparing(RoomInfo::id)));
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
              new ListRoomsRequest().offset(offset).limit(limit),
              Comparator.<RoomInfo, Long>comparing(RoomInfo::id)));
    }

    return builder.build();
  }
}
