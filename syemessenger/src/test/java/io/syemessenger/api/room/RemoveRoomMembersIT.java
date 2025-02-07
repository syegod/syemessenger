package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.api.room.RoomAssertions.joinRoom;
import static io.syemessenger.environment.AssertionUtils.assertCollections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class RemoveRoomMembersIT {

  private static RoomInfo existingRoomInfo;

  @BeforeAll
  static void beforeAll(AccountInfo accountInfo) {
    existingRoomInfo = createRoom(accountInfo);
  }

  @Test
  void testRemoveRoomMembersNotLoggedIn(ClientSdk clientSdk) {
    try {
      clientSdk.roomSdk().removeRoomMembers(new RemoveMembersRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testRemoveRoomMembersFailedMethodSource")
  void testRemoveRoomMembersFailed(FailedArgs args, ClientSdk clientSdk) {
    login(clientSdk, request -> request.username(existingRoomInfo.owner()));
    try {
      clientSdk.roomSdk().removeRoomMembers(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, RemoveMembersRequest request, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testRemoveRoomMembersFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "No room id",
            new RemoveMembersRequest().memberIds(10L),
            400,
            "Missing or invalid: roomId"),
        new FailedArgs(
            "Wrong room id",
            new RemoveMembersRequest().roomId(Long.MAX_VALUE).memberIds(10L),
            404,
            "Room not found"),
        new FailedArgs(
            "Empty member list",
            new RemoveMembersRequest().roomId(100L).memberIds(List.of()),
            400,
            "Missing or invalid: memberIds"));
  }

  @Test
  void testRemoveRoomMembersRemoveOwner(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    try {
      clientSdk
          .roomSdk()
          .removeRoomMembers(
              new RemoveMembersRequest().roomId(roomInfo.id()).memberIds(accountInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot remove room owner");
    }
  }

  @Test
  void testRemoveRoomMembersNotOwner(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    clientSdk.roomSdk().joinRoom(roomInfo.name());

    try {
      clientSdk
          .roomSdk()
          .removeRoomMembers(
              new RemoveMembersRequest().roomId(roomInfo.id()).memberIds(accountInfo.id()));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not room owner");
    }
  }

  @Test
  void testRemoveRoomMembers(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    clientSdk.roomSdk().joinRoom(roomInfo.name());

    login(clientSdk, accountInfo);

    clientSdk
        .roomSdk()
        .removeRoomMembers(
            new RemoveMembersRequest().roomId(roomInfo.id()).memberIds(anotherAccountInfo.id()));

    final var roomResponse =
        clientSdk.roomSdk().getRoomMembers(new GetRoomMembersRequest().roomId(roomInfo.id()));
    assertEquals(1, roomResponse.totalCount());
  }

  @Test
  void testRemoveMultipleRoomMembers(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);

    int n = 10;
    final var members = new ArrayList<AccountInfo>();
    for (int i = 0; i < n; i++) {
      final var account = createAccount();
      joinRoom(roomInfo.name(), account.username());
      members.add(account);
    }

    login(clientSdk, accountInfo);

    clientSdk
        .roomSdk()
        .removeRoomMembers(
            new RemoveMembersRequest()
                .roomId(roomInfo.id())
                .memberIds(members.stream().map(AccountInfo::id).toList()));

    final var roomResponse =
        clientSdk.roomSdk().getRoomMembers(new GetRoomMembersRequest().roomId(roomInfo.id()));
    assertEquals(1, roomResponse.totalCount());
  }
}
