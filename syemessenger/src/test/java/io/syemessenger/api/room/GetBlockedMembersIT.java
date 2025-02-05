package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.api.room.RoomAssertions.joinRoom;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static io.syemessenger.environment.AssertionUtils.getFields;
import static io.syemessenger.environment.AssertionUtils.toComparator;
import static io.syemessenger.environment.IntegrationEnvironment.cleanTables;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
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
public class GetBlockedMembersIT {

  @BeforeEach
  void beforeEach(DataSource dataSource) {
    cleanTables(dataSource);
  }

  @Test
  void testGetBlockedMembersNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      clientSdk.roomSdk().getBlockedMembers(new GetBlockedMembersRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetBlockedMembersFailedMethodSource")
  void testGetBlockedMembersFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      createRoom(accountInfo);
      login(clientSdk, accountInfo);
      clientSdk.roomSdk().getBlockedMembers(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, GetBlockedMembersRequest request, int errorCode, String errorMessage) {}

  private static Stream<?> testGetBlockedMembersFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "No room id", new GetBlockedMembersRequest(), 400, "Missing or invalid: roomId"),
        new FailedArgs(
            "Non-existing room id",
            new GetBlockedMembersRequest().roomId(Long.MAX_VALUE),
            404,
            "Room not found"),
        new FailedArgs(
            "Offset is negative",
            new GetBlockedMembersRequest().roomId(10L).offset(-50),
            400,
            "Missing or invalid: offset"),
        new FailedArgs(
            "Limit is negative",
            new GetBlockedMembersRequest().roomId(10L).limit(-50),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Limit is over than max",
            new GetBlockedMembersRequest().roomId(10L).limit(60),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Order by non-existing field",
            new GetBlockedMembersRequest()
                .roomId(10L)
                .orderBy(new OrderBy().field(randomAlphanumeric(10))),
            400,
            "Missing or invalid: orderBy"));
  }

  @Test
  void testGetBlockedMembersNotRoomOwner(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    try {
      clientSdk.roomSdk().getBlockedMembers(new GetBlockedMembersRequest().roomId(roomInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not room owner");
    }
  }

  @SuppressWarnings("unchecked")
  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetBlockedMembersMethodSource")
  void testGetBlockedMembers(SuccessArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    final var request = args.request;

    int n = 25;
    final var offset = request.offset() != null ? request.offset() : 0;
    final var limit = request.limit() != null ? request.limit() : 50;
    final var roomMembers = new ArrayList<AccountInfo>();
    for (int i = 0; i < n; i++) {
      final var account = createAccount();
      joinRoom(roomInfo.name(), account.username());
      roomMembers.add(account);
    }

    clientSdk
        .roomSdk()
        .blockRoomMembers(
            new BlockMembersRequest()
                .roomId(roomInfo.id())
                .memberIds(roomMembers.stream().map(AccountInfo::id).toList()));

    final var expectedBlockedMembers =
        roomMembers.stream().sorted(args.comparator).skip(offset).limit(limit).toList();

    final var response = clientSdk.roomSdk().getBlockedMembers(request);
    assertEquals(roomMembers.size(), response.totalCount(), "totalCount");
    assertCollections(
        expectedBlockedMembers, response.accountInfos(), AccountAssertions::assertAccount);
  }

  @SuppressWarnings("rawtypes")
  private record SuccessArgs(
      String test, GetBlockedMembersRequest request, Comparator comparator) {}

  private static Stream<?> testGetBlockedMembersMethodSource() {
    final var builder = Stream.<SuccessArgs>builder();

    final String[] fields = getFields(AccountInfo.class);
    final Direction[] directions = {Direction.ASC, Direction.DESC, null};

    // Sort by fields

    for (String field : fields) {
      for (Direction direction : directions) {
        final var orderBy = new OrderBy().field(field).direction(direction);
        builder.add(
            new SuccessArgs(
                "Field" + field + ", direction: " + direction,
                new GetBlockedMembersRequest().roomId(1L).orderBy(orderBy),
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
              new GetBlockedMembersRequest().roomId(1L).offset(offset).limit(limit),
              Comparator.<AccountInfo, Long>comparing(AccountInfo::id)));
    }

    return builder.build();
  }
}
