package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.api.room.RoomAssertions.joinRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class UnblockRoomMembersIT {

  @Test
  void testUnblockRoomMembersNotLoggedIn(ClientSdk clientSdk) {

    try {
      clientSdk.roomSdk().unblockRoomMembers(new UnblockMembersRequest());
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testUnblockRoomMembersFailedMethodSource")
  void testUnblockRoomMembersFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);

    try {
      clientSdk.roomSdk().unblockRoomMembers(args.request);
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, UnblockMembersRequest request, int errorCode, String errorMessage) {}

  private static Stream<?> testUnblockRoomMembersFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "No room id",
            new UnblockMembersRequest().memberIds(50L, 50L),
            400,
            "Missing or invalid: roomId"),
        new FailedArgs(
            "Wrong room id",
            new UnblockMembersRequest().roomId(Long.MAX_VALUE).memberIds(50L, 50L),
            404,
            "Room not found"),
        new FailedArgs(
            "Empty member id list",
            new UnblockMembersRequest().roomId(Long.MAX_VALUE).memberIds(List.of()),
            400,
            "Missing or invalid: memberIds"),
        new FailedArgs(
            "Null member id list",
            new UnblockMembersRequest().roomId(Long.MAX_VALUE),
            400,
            "Missing or invalid: memberIds"));
  }

  @Test
  void testUnblockMembersNotOwner(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);

    try {
      clientSdk
          .roomSdk()
          .unblockRoomMembers(
              new UnblockMembersRequest().roomId(roomInfo.id()).memberIds(50L, 50L));
      fail("Exception expected");
    } catch (Exception ex) {
      assertError(ex, 403, "Not room owner");
    }
  }

  @Test
  void testUnblockMembers(ClientSdk clientSdk, AccountInfo accountInfo) {
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

    clientSdk
        .roomSdk()
        .unblockRoomMembers(
            new UnblockMembersRequest()
                .roomId(roomInfo.id())
                .memberIds(expectedBlockedMembers.stream().map(AccountInfo::id).toList()));

    final var blockedMembersResponse =
        clientSdk.roomSdk().getBlockedMembers(new GetBlockedMembersRequest().roomId(roomInfo.id()));
    assertEquals(0, blockedMembersResponse.totalCount(), "totalCount");
    assertTrue(blockedMembersResponse.accountInfos().isEmpty());
  }
}
