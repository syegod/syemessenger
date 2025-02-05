package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.assertAccount;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.api.room.RoomAssertions.joinRoom;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountAssertions;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestClassOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class BlockRoomMembersIT {

  @Test
  void testBlockRoomMembersNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);

    try {
      clientSdk.roomSdk().blockRoomMembers(new BlockMembersRequest());
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testBlockRoomMembersFailedMethodSource")
  void testBlockRoomMembersFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);

    try {
      clientSdk.roomSdk().blockRoomMembers(args.request);
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, BlockMembersRequest request, int errorCode, String errorMessage) {}

  private static Stream<?> testBlockRoomMembersFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "No room id",
            new BlockMembersRequest().memberIds(50L, 50L),
            400,
            "Missing or invalid: roomId"),
        new FailedArgs(
            "Wrong room id",
            new BlockMembersRequest().roomId(Long.MAX_VALUE).memberIds(50L, 50L),
            404,
            "Room not found"),
        new FailedArgs(
            "Empty member id list",
            new BlockMembersRequest().roomId(Long.MAX_VALUE).memberIds(List.of()),
            400,
            "Missing or invalid: memberIds"),
        new FailedArgs(
            "Null member id list",
            new BlockMembersRequest().roomId(Long.MAX_VALUE),
            400,
            "Missing or invalid: memberIds"));
  }

  @Test
  void testBlockMembersNotOwner(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);

    try {
      clientSdk
          .roomSdk()
          .blockRoomMembers(new BlockMembersRequest().roomId(roomInfo.id()).memberIds(50L, 50L));
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, 403, "Not room owner");
    }
  }

  @Test
  void testBlockMembersBlockOwner(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    try {
      clientSdk
          .roomSdk()
          .blockRoomMembers(
              new BlockMembersRequest().roomId(roomInfo.id()).memberIds(accountInfo.id()));
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot block room owner");
    }
  }

  @Test
  void testBlockMembersWrongMemberIds(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    try {
      clientSdk
          .roomSdk()
          .blockRoomMembers(
              new BlockMembersRequest().roomId(roomInfo.id()).memberIds(5000L, 10000L));
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, 404, "Account not found");
    }
  }

  @Test
  void testBlockMembersNotMember(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    clientSdk
        .roomSdk()
        .blockRoomMembers(
            new BlockMembersRequest().roomId(roomInfo.id()).memberIds(anotherAccountInfo.id()));

    final var blockedMembers =
        clientSdk.roomSdk().getBlockedMembers(new GetBlockedMembersRequest().roomId(roomInfo.id()));

    assertEquals(1, blockedMembers.totalCount(), "totalCount");
    assertAccount(anotherAccountInfo, blockedMembers.accountInfos().getFirst());
  }

  @Test
  void testBlockMembers(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    int n = 25;
    final var expectedBlockedMembers = new ArrayList<AccountInfo>();
    for (int i = 0; i < n; i++) {
      final var account = createAccount();
      joinRoom(roomInfo.name(), account.username());
      expectedBlockedMembers.add(account);
    }

    clientSdk
        .roomSdk()
        .blockRoomMembers(
            new BlockMembersRequest()
                .roomId(roomInfo.id())
                .memberIds(expectedBlockedMembers.stream().map(AccountInfo::id).toList()));

    final var blockedMembersResponse =
        clientSdk.roomSdk().getBlockedMembers(new GetBlockedMembersRequest().roomId(roomInfo.id()));
    assertEquals(expectedBlockedMembers.size(), blockedMembersResponse.totalCount(), "totalCount");
    assertCollections(
        expectedBlockedMembers,
        blockedMembersResponse.accountInfos(),
        AccountAssertions::assertAccount);
  }
}
