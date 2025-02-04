package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
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
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class JoinRoomIT {

  //  private ClientSdk clientSdk;
  //  private AccountSdk accountSdk;
  //  private RoomSdk roomSdk;
  //  private AccountInfo existingAccountInfo;
  //  private RoomInfo existingRoomInfo;
  //  private AccountInfo anotherAccountInfo;
  //
  //  @BeforeEach
  //  void beforeEach() {
  //    clientSdk = new ClientSdk();
  //    accountSdk = clientSdk.api(AccountSdk.class);
  //    roomSdk = clientSdk.api(RoomSdk.class);
  //    existingAccountInfo = createAccount();
  //    anotherAccountInfo = createAccount();
  //    existingRoomInfo = createRoom(existingAccountInfo);
  //  }

  @Test
  void testJoinRoomNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      clientSdk.api(RoomSdk.class).joinRoom(roomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testJoinRoomFailedMethodSource")
  void testJoinRoomFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    clientSdk
        .api(AccountSdk.class)
        .login(new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      clientSdk.api(RoomSdk.class).joinRoom(args.name);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(String test, String name, int errorCode, String errorMessage) {}

  private static Stream<?> testJoinRoomFailedMethodSource() {
    return Stream.of(
        new FailedArgs("Room name blank", "", 400, "Missing or invalid: name"),
        new FailedArgs("Null room name", null, 400, "Missing or invalid: name"),
        new FailedArgs("Wrong room name", randomAlphanumeric(20), 404, "Room not found"));
  }

  @Test
  void testJoinOwnRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      clientSdk
          .api(AccountSdk.class)
          .login(new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
      final var roomInfo = createRoom(accountInfo);
      clientSdk.api(RoomSdk.class).joinRoom(roomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot join room: already joined");
    }
  }

  @Test
  void testJoinRoomRepeat(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);

    clientSdk
        .api(AccountSdk.class)
        .login(
            new LoginAccountRequest()
                .username(anotherAccountInfo.username())
                .password("test12345"));

    final var roomSdk = clientSdk.api(RoomSdk.class);
    roomSdk.joinRoom(roomInfo.name());
    try {
      roomSdk.joinRoom(roomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot join room: already joined");
    }
  }

  @Disabled("https://github.com/syegod/syemessenger/issues/16")
  @Test
  void testJoinRoomBlocked(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var accountSdk = clientSdk.api(AccountSdk.class);
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var roomInfo = createRoom(accountInfo);

    final var roomSdk = clientSdk.api(RoomSdk.class);
    roomSdk.blockRoomMembers(
        new BlockMembersRequest()
            .roomId(roomInfo.id())
            .memberIds(List.of(anotherAccountInfo.id())));

    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    try {
      roomSdk.joinRoom(roomInfo.name());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot join room: blocked");
    }
  }

  @Test
  void testJoinRoom(ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var accountSdk = clientSdk.api(AccountSdk.class);
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    final var existingRoomInfo = createRoom(accountInfo);

    accountSdk.login(
        new LoginAccountRequest().username(anotherAccountInfo.username()).password("test12345"));

    clientSdk.api(RoomSdk.class).joinRoom(existingRoomInfo.name());

    final var roomsResponse = accountSdk.getRooms(new GetRoomsRequest());
    assertNotNull(roomsResponse.roomInfos(), "roomInfos");
    assertEquals(1, roomsResponse.roomInfos().size());
    final var roomInfo = roomsResponse.roomInfos().getFirst();
    assertRoom(existingRoomInfo, roomInfo);
  }
}
