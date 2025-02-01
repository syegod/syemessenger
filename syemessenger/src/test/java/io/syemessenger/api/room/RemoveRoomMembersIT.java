package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.LoginAccountRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class RemoveRoomMembersIT {

  private static AccountInfo existingAccountInfo;
  private static AccountInfo anotherAccountInfo;
  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;
  private RoomInfo existingRoomInfo;

  @BeforeAll
  static void beforeAll() {
    existingAccountInfo = createAccount();
    anotherAccountInfo = createAccount();
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);

    existingAccountInfo = createAccount();
    anotherAccountInfo = createAccount();

    existingRoomInfo = createRoom(existingAccountInfo);
  }

  @AfterEach
  void afterEach() {
    if (clientSdk != null) {
      clientSdk.close();
    }
  }

  @Test
  void testRemoveRoomMembersNotLoggedIn() {
    try {
      roomSdk.removeRoomMembers(new RemoveMembersRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testRemoveRoomMembersFailedMethodSource")
  void testRemoveRoomMembersFailed(
      String test, RemoveMembersRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    try {
      roomSdk.removeRoomMembers(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  static Stream<Arguments> testRemoveRoomMembersFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "No room id",
            new RemoveMembersRequest().memberIds(List.of(anotherAccountInfo.id())),
            400,
            "Missing or invalid: roomId"),
        Arguments.of(
            "Wrong room id",
            new RemoveMembersRequest().roomId(1000L).memberIds(List.of(anotherAccountInfo.id())),
            404,
            "Room not found"),
        Arguments.of(
            "Empty member list",
            new RemoveMembersRequest()
                .roomId(existingAccountInfo.id())
                .memberIds(new ArrayList<>()),
            400,
            "Missing or invalid: memberIds")
        // TODO: resolve
        //        Arguments.of(
        //            "Wrong member id",
        //            new
        // RemoveMembersRequest().roomId(existingAccountInfo.id()).memberIds(List.of(1000L)),
        //            404,
        //            ""),
        );
  }

  @Test
  void testRemoveRoomMembersRemoveOwner() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    try {
      roomSdk.removeRoomMembers(
          new RemoveMembersRequest()
              .roomId(existingRoomInfo.id())
              .memberIds(List.of(existingAccountInfo.id())));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot remove room owner");
    }
  }

  @Test
  void testRemoveRoomMembersNotOwner() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    roomSdk.joinRoom(existingRoomInfo.name());

    try {
      roomSdk.removeRoomMembers(
          new RemoveMembersRequest()
              .roomId(existingRoomInfo.id())
              .memberIds(List.of(anotherAccountInfo.id())));
    } catch (Exception ex) {
      assertError(ex, 403, "Not room owner");
    }
  }

  @Test
  void testRemoveRoomMembers() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    roomSdk.joinRoom(existingRoomInfo.name());

    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));

    roomSdk.removeRoomMembers(
        new RemoveMembersRequest()
            .roomId(existingRoomInfo.id())
            .memberIds(List.of(anotherAccountInfo.id())));

    final var roomResponse =
        roomSdk.getRoomMembers(new GetRoomMembersRequest().roomId(existingRoomInfo.id()));
    assertEquals(1, roomResponse.totalCount());
  }
}
