package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.GetRoomsRequest;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class LeaveRoomIT {

  @Test
  void testLeaveRoomNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      clientSdk.roomSdk().leaveRoom(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testLeaveRoomFailedMethodSource")
  void testLeaveRoomFailed(
      FailedArgs args,
      ClientSdk clientSdk,
      AccountInfo accountInfo,
      AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, request -> request.username(anotherAccountInfo.username()));
    clientSdk.roomSdk().joinRoom(roomInfo.name());

    try {
      clientSdk.roomSdk().leaveRoom(args.id);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(String test, Long id, int errorCode, String errorMessage) {}

  private static Stream<?> testLeaveRoomFailedMethodSource() {
    return Stream.of(
        new FailedArgs("No id", null, 400, "Missing or invalid: id"),
        new FailedArgs("Wrong id", 123L, 404, "Room not found"));
  }

  @Test
  void testLeaveOwnRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, request -> request.username(accountInfo.username()));
    clientSdk.roomSdk().leaveRoom(roomInfo.id());

    final var roomsResponse = clientSdk.accountSdk().getRooms(new GetRoomsRequest());
    assertEquals(0, roomsResponse.totalCount());
  }

  @Test
  void testLeaveRoomNotJoined(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccount) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, request -> request.username(anotherAccount.username()));

    try {
      clientSdk.roomSdk().leaveRoom(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot leave room: not joined");
    }
  }

  @Test
  void testLeaveRoomBlocked(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, request -> request.username(anotherAccountInfo.username()));
    clientSdk.roomSdk().joinRoom(roomInfo.name());

    login(clientSdk, request -> request.username(accountInfo.username()));

    clientSdk
        .roomSdk()
        .blockRoomMembers(
            new BlockMembersRequest()
                .roomId(roomInfo.id())
                .memberIds(List.of(anotherAccountInfo.id())));

    login(clientSdk, request -> request.username(anotherAccountInfo.username()));

    try {
      clientSdk.roomSdk().leaveRoom(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot leave room: not joined");
    }
  }

  @Test
  void testLeaveRoomRepeat(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, request -> request.username(anotherAccountInfo.username()));
    clientSdk.roomSdk().joinRoom(roomInfo.name());
    clientSdk.roomSdk().leaveRoom(roomInfo.id());

    try {
      clientSdk.roomSdk().leaveRoom(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Cannot leave room: not joined");
    }
  }

  @Test
  void testLeaveRoom(ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, request -> request.username(anotherAccountInfo.username()));

    clientSdk.roomSdk().joinRoom(roomInfo.name());
    clientSdk.roomSdk().leaveRoom(roomInfo.id());

    final var roomsResponse = clientSdk.accountSdk().getRooms(new GetRoomsRequest());
    assertNotNull(roomsResponse.roomInfos(), "roomInfos");
    assertEquals(0, roomsResponse.roomInfos().size());
  }
}
