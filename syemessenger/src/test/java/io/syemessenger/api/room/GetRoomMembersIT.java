package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.api.room.RoomAssertions.joinRoom;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static io.syemessenger.environment.AssertionUtils.getFields;
import static io.syemessenger.environment.AssertionUtils.toComparator;
import static io.syemessenger.environment.CounterUtils.nextLong;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.OrderBy;
import io.syemessenger.api.OrderBy.Direction;
import io.syemessenger.api.account.AccountAssertions;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.GetRoomsRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.OffsetLimit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class GetRoomMembersIT {

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;
  private static AccountInfo accountInfo;
  private static RoomInfo existingRoomInfo;

  @BeforeAll
  static void beforeAll() {
    accountInfo = createAccount();
    existingRoomInfo = createRoom(accountInfo);
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);
  }

  @AfterEach
  void afterEach() {
    CloseHelper.close(clientSdk);
  }

  @Test
  void testGetRoomMembersNotLoggedIn() {
    try {
      roomSdk.getRoomMembers(new GetRoomMembersRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomMembersFailedMethodSource")
  void testGetRoomMembersFailed(
      String test, GetRoomMembersRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      roomSdk.getRoomMembers(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testGetRoomMembersFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Missing roomId",
            new GetRoomMembersRequest().roomId(null),
            400,
            "Missing or invalid: roomId"),
        Arguments.of(
            "Offset is negative",
            new GetRoomMembersRequest().roomId(existingRoomInfo.id()).offset(-50),
            400,
            "Missing or invalid: offset"),
        Arguments.of(
            "Limit is negative",
            new GetRoomMembersRequest().roomId(existingRoomInfo.id()).limit(-50),
            400,
            "Missing or invalid: limit"),
        Arguments.of(
            "Limit is over than max",
            new GetRoomMembersRequest().roomId(existingRoomInfo.id()).limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomMembersMethodSource")
  void testGetRoomMembers(
      String test, GetRoomMembersRequest request, Comparator<Object> comparator) {
    final int n = 25;
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;

    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var roomInfo = createRoom(accountInfo);
    request.roomId(roomInfo.id());
    final var roomMembers = new ArrayList<AccountInfo>();
    roomMembers.add(accountInfo);
    for (int i = 0; i < n; i++) {
      final var account = createAccount(r -> r.username("username@" + nextLong()));
      joinRoom(roomInfo.name(), account.username());
      roomMembers.add(account);
    }

    final var expectedRoomMembers =
        roomMembers.stream().sorted(comparator).skip(offset).limit(limit).toList();

    final var response = roomSdk.getRoomMembers(request);
    //TODO: figure out what is expected total count
    assertEquals(roomMembers.size(), response.totalCount(), "totalCount");
    assertCollections(
        expectedRoomMembers, response.accountInfos(), AccountAssertions::assertAccount);
  }

  private static Stream<Arguments> testGetRoomMembersMethodSource() {
    final var builder = Stream.<Arguments>builder();

    final String[] fields = getFields(AccountInfo.class);
    final Direction[] directions = {Direction.ASC, Direction.DESC, null};

    // Sort by fields
    for (String field : fields) {
      for (Direction direction : directions) {
        final var orderBy = new OrderBy().field(field).direction(direction);
        builder.add(
            Arguments.of(
                "Field: " + field + ", direction: " + direction,
                new GetRoomMembersRequest().orderBy(orderBy),
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
      new OffsetLimit(20, 10),
    };

    for (OffsetLimit offsetLimit : offsetLimits) {
      final var offset = offsetLimit.offset();
      final var limit = offsetLimit.limit();
      builder.add(
          Arguments.of(
              "Offset: " + offset + ", limit: " + limit,
              new GetRoomMembersRequest().offset(offset).limit(limit),
              Comparator.<AccountInfo, Long>comparing(AccountInfo::id)));
    }

    return builder.build();
  }
}
