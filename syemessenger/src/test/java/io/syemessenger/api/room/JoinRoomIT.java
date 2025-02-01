package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.assertRoom;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.GetRoomsRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironment;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class JoinRoomIT {

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;
  private AccountInfo existingAccountInfo;
  private RoomInfo existingRoomInfo;
  private AccountInfo anotherAccountInfo;

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
    CloseHelper.close(clientSdk);
  }

  @Test
  void testJoinRoomNotLoggedIn() {
    try {
      roomSdk.joinRoom(existingRoomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testJoinRoomFailedMethodSource")
  void testJoinRoomFailed(String test, String name, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    try {
      roomSdk.joinRoom(name);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testJoinRoomFailedMethodSource() {
    return Stream.of(
        Arguments.of("Room name blank", "", 400, "Missing or invalid: name"),
        Arguments.of("Null room name", null, 400, "Missing or invalid: name"),
        Arguments.of("Wrong room name", randomAlphanumeric(20), 404, "Room not found"));
  }

  @Test
  void testJoinOwnRoom() {
    try {
      accountSdk.login(
          new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
      roomSdk.joinRoom(existingRoomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot join room: already joined");
    }
  }

  @Test
  void testJoinRoomRepeat() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    try {
      roomSdk.joinRoom(existingRoomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot join room: already joined");
    }
  }

  @Test
  void testJoinRoomBlocked() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    roomSdk.blockRoomMembers(
        new BlockMembersRequest()
            .roomId(existingRoomInfo.id())
            .memberIds(List.of(anotherAccountInfo.id())));
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    try {
      roomSdk.joinRoom(existingRoomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot join room: blocked");
    }
  }

  @Test
  void testJoinRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    final var roomsResponse = accountSdk.getRooms(new GetRoomsRequest());
    assertNotNull(roomsResponse.roomInfos(), "roomInfos");
    assertEquals(1, roomsResponse.roomInfos().size());
    final var roomInfo = roomsResponse.roomInfos().getFirst();
    assertRoom(existingRoomInfo, roomInfo);
  }
}
