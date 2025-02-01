package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static io.syemessenger.environment.AssertionUtils.toComparator;
import static io.syemessenger.environment.CounterUtils.nextLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.OrderBy;
import io.syemessenger.api.OrderBy.Direction;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironment;
import io.syemessenger.environment.OffsetLimit;
import java.util.Comparator;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ListRoomsIT {

  private static IntegrationEnvironment environment;

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;
  private AccountInfo accountInfo;

  @BeforeAll
  static void beforeAll() {
    environment = new IntegrationEnvironment();
    environment.start();
  }

  @AfterAll
  static void afterAll() {
    CloseHelper.close(environment);
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);
    accountInfo = createAccount();
  }

  @AfterEach
  void afterEach() {
    CloseHelper.close(clientSdk);
  }

  @Test
  void testListRoomsNotLoggedIn() {
    try {
      roomSdk.listRooms(new ListRoomsRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testListRoomsFailedMethodSource")
  void testListRoomsFailed(
      String test, ListRoomsRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      roomSdk.listRooms(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testListRoomsFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Offset is negative",
            new ListRoomsRequest().offset(-50),
            400,
            "Missing or invalid: offset"),
        Arguments.of(
            "Limit is negative",
            new ListRoomsRequest().limit(-50),
            400,
            "Missing or invalid: limit"),
        Arguments.of(
            "Limit is over than max",
            new ListRoomsRequest().limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testListRoomsMethodSource")
  void testListRooms(String test, ListRoomsRequest request, Comparator<Object> comparator) {
    final int n = 20;
    final var k = request.keyword();
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;

    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var expectedRoomInfos =
        IntStream.range(0, n)
            .mapToObj(
                v -> {
                  final var l = nextLong();
                  return roomSdk.createRoom(
                      new CreateRoomRequest().name("room@" + l).description("description@" + l));
                })
            .filter(
                roomInfo -> {
                  if (k != null) {
                    return roomInfo.name().contains(k) || roomInfo.description().contains(k);
                  } else {
                    return true;
                  }
                })
            .sorted(comparator)
            .skip(offset)
            .limit(limit)
            .toList();

    final var response = roomSdk.listRooms(request);
    assertEquals(expectedRoomInfos.size(), response.totalCount(), "totalCount");
    assertCollections(expectedRoomInfos, response.roomInfos(), RoomAssertions::assertRoom);
  }

  private static Stream<Arguments> testListRoomsMethodSource() {
    final var builder = Stream.<Arguments>builder();

    final String[] fields = {"id", "name"};
    final Direction[] directions = {Direction.ASC, Direction.DESC, null};
    final String[] keywords = {"room@", "description@", null};
    final OffsetLimit[] offsetLimits = {
      new OffsetLimit(null, null),
      new OffsetLimit(null, 5),
      new OffsetLimit(10, null),
      new OffsetLimit(5, 10),
      new OffsetLimit(10, 5)
    };

    for (String field : fields) {
      for (Direction direction : directions) {
        final var orderBy = new OrderBy().field(field).direction(direction);
        final var comparator = toComparator(orderBy);
        for (String keyword : keywords) {
          for (OffsetLimit offsetLimit : offsetLimits) {
            builder.add(
                Arguments.of(
                    "test",
                    new ListRoomsRequest()
                        .keyword(keyword)
                        .offset(offsetLimit.offset())
                        .limit(offsetLimit.limit())
                        .orderBy(orderBy),
                    comparator));
          }
        }
      }
    }

    return builder.build();
  }
}
