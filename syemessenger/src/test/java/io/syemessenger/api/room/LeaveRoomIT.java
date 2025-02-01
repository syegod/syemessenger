package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.GetRoomsRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LeaveRoomIT {

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;
  private AccountInfo existingAccountInfo;
  private AccountInfo anotherAccountInfo;
  private RoomInfo existingRoomInfo;

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
  void testLeaveRoomNotLoggedIn() {
    try {
      roomSdk.leaveRoom(existingRoomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testLeaveRoomFailedMethodSource")
  void testLeaveRoomFailed(String test, Long id, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    try {
      roomSdk.leaveRoom(id);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  static Stream<Arguments> testLeaveRoomFailedMethodSource() {
    return Stream.of(
        Arguments.of("No id", null, 400, "Missing or invalid: id"),
        Arguments.of("Wrong id", 123L, 404, "Room not found"));
  }

  @Test
  void testLeaveOwnRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    roomSdk.leaveRoom(existingRoomInfo.id());
    final var roomsResponse = accountSdk.getRooms(new GetRoomsRequest());
    assertEquals(0, roomsResponse.totalCount());
  }

  @Test
  void testLeaveRoomNotJoined() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));
    try {
      roomSdk.leaveRoom(existingRoomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot leave room: not joined");
    }
  }

  @Test
  void testLeaveRoomBlocked() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    roomSdk.joinRoom(existingRoomInfo.name());

    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));

    roomSdk.blockRoomMembers(
        new BlockMembersRequest()
            .roomId(existingRoomInfo.id())
            .memberIds(List.of(anotherAccountInfo.id())));

    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    try {
      roomSdk.leaveRoom(existingRoomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot leave room: not joined");
    }
  }

  @Test
  void testLeaveRoomRepeat() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    roomSdk.leaveRoom(existingRoomInfo.id());
    try {
      roomSdk.leaveRoom(existingRoomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot leave room: not joined");
    }
  }

  @Test
  void testLeaveRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    roomSdk.leaveRoom(existingRoomInfo.id());
    final var roomsResponse = accountSdk.getRooms(new GetRoomsRequest());
    assertNotNull(roomsResponse.roomInfos(), "roomInfos");
    assertEquals(0, roomsResponse.roomInfos().size());
  }
}
