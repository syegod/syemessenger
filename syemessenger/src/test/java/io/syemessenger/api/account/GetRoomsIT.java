package io.syemessenger.api.account;

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
import io.syemessenger.api.room.CreateRoomRequest;
import io.syemessenger.api.room.RoomAssertions;
import io.syemessenger.api.room.RoomInfo;
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
public class GetRoomsIT {

  @AfterEach
  void afterEach(DataSource dataSource) {
    cleanTables(dataSource);
  }

  @Test
  void testGetRoomsNotLoggedIn(ClientSdk clientSdk) {
    try {
      clientSdk.accountSdk().getRooms(new GetRoomsRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testGetRoomsFailedMethodSource")
  void testGetRoomsFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.accountSdk().getRooms(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, GetRoomsRequest request, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testGetRoomsFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Offset is negative",
            new GetRoomsRequest().offset(-50),
            400,
            "Missing or invalid: offset"),
        new FailedArgs(
            "Limit is negative",
            new GetRoomsRequest().limit(-50),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Limit is over than max",
            new GetRoomsRequest().limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomsMethodSource")
  void testGetRooms(SuccessArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    final int n = 25;
    final var request = args.request;
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
            .sorted(args.comparator)
            .toList();

    final var expectedRoomInfos = roomInfos.stream().skip(offset).limit(limit).toList();

    final var response = clientSdk.accountSdk().getRooms(request);
    assertEquals(roomInfos.size(), response.totalCount(), "totalCount");
    assertCollections(expectedRoomInfos, response.roomInfos(), RoomAssertions::assertRoom);
  }

  @SuppressWarnings("rawtypes")
  private record SuccessArgs(String test, GetRoomsRequest request, Comparator comparator) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testGetRoomsMethodSource() {
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
                new GetRoomsRequest().orderBy(orderBy),
                toComparator(orderBy)));
      }
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
              new GetRoomsRequest().offset(offset).limit(limit),
              Comparator.<RoomInfo, Long>comparing(RoomInfo::id)));
    }

    return builder.build();
  }

  @Test
  void testGetRoomsEmpty(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    final var rooms = clientSdk.accountSdk().getRooms(new GetRoomsRequest());
    assertEquals(0, rooms.roomInfos().size());
  }
}
