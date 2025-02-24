package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.api.room.RoomAssertions.joinRoom;
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
import io.syemessenger.api.account.AccountAssertions;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import io.syemessenger.environment.OffsetLimit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class GetRoomMembersIT {

  @BeforeEach
  void beforeEach(DataSource dataSource) {
    cleanTables(dataSource);
  }

  @Test
  void testGetRoomMembersNotLoggedIn(ClientSdk clientSdk) {
    try {
      clientSdk.roomSdk().getRoomMembers(new GetRoomMembersRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomMembersFailedMethodSource")
  void testGetRoomMembersFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.roomSdk().getRoomMembers(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, GetRoomMembersRequest request, int errorCode, String errorMessage) {}

  private static Stream<?> testGetRoomMembersFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Missing roomId",
            new GetRoomMembersRequest().roomId(null),
            400,
            "Missing or invalid: roomId"),
        new FailedArgs(
            "Offset is negative",
            new GetRoomMembersRequest().roomId(10L).offset(-50),
            400,
            "Missing or invalid: offset"),
        new FailedArgs(
            "Limit is negative",
            new GetRoomMembersRequest().roomId(10L).limit(-50),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Limit is over than max",
            new GetRoomMembersRequest().roomId(10L).limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @Test
  void testGetRoomMembersNotMember(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    try {
      clientSdk.roomSdk().getRoomMembers(new GetRoomMembersRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not a room member");
    }
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomMembersMethodSource")
  void testGetRoomMembers(SuccessArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    final int n = 25;
    final var request = args.request;
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;

    login(clientSdk, accountInfo);

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
        roomMembers.stream().sorted(args.comparator).skip(offset).limit(limit).toList();

    final var response = clientSdk.roomSdk().getRoomMembers(request);
    assertEquals(roomMembers.size(), response.totalCount(), "totalCount");
    assertCollections(
        expectedRoomMembers, response.accountInfos(), AccountAssertions::assertAccount);
  }

  private record SuccessArgs(String test, GetRoomMembersRequest request, Comparator comparator) {

    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testGetRoomMembersMethodSource() {
    final var builder = Stream.<SuccessArgs>builder();

    final String[] fields = getFields(AccountInfo.class);
    final Direction[] directions = {Direction.ASC, Direction.DESC, null};

    // Sort by fields

    for (String field : fields) {
      for (Direction direction : directions) {
        final var orderBy = new OrderBy().field(field).direction(direction);
        builder.add(
            new SuccessArgs(
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
          new SuccessArgs(
              "Offset: " + offset + ", limit: " + limit,
              new GetRoomMembersRequest().offset(offset).limit(limit),
              Comparator.<AccountInfo, Long>comparing(AccountInfo::id)));
    }

    return builder.build();
  }
}
