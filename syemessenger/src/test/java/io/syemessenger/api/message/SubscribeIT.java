package io.syemessenger.api.message;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.room.BlockMembersRequest;
import io.syemessenger.api.room.RemoveMembersRequest;
import io.syemessenger.api.room.RoomInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class SubscribeIT {

  @Test
  void testSubscribeNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    try {
      clientSdk.messageSdk().subscribe(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testSubscribeNotExistingRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.messageSdk().subscribe(Long.MAX_VALUE);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 404, "Room not found");
    }
  }

  @Test
  void testSubscribeNotMember(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      login(clientSdk, anotherAccountInfo);
      clientSdk.messageSdk().subscribe(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: not a member");
    }
  }

  @Test
  void testSubscribeBlocked(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      login(clientSdk, anotherAccountInfo);
      clientSdk.roomSdk().joinRoom(roomInfo.name());
      login(clientSdk, accountInfo);
      clientSdk
          .roomSdk()
          .blockRoomMembers(
              new BlockMembersRequest().memberIds(anotherAccountInfo.id()).roomId(roomInfo.id()));
      login(clientSdk, anotherAccountInfo);
      clientSdk.messageSdk().subscribe(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: blocked");
    }
  }

  @Test
  void testSubscribeRemoved(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      login(clientSdk, anotherAccountInfo);
      clientSdk.roomSdk().joinRoom(roomInfo.name());
      login(clientSdk, accountInfo);
      clientSdk
          .roomSdk()
          .removeRoomMembers(
              new RemoveMembersRequest().memberIds(anotherAccountInfo.id()).roomId(roomInfo.id()));
      login(clientSdk, anotherAccountInfo);
      clientSdk.messageSdk().subscribe(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: not a member");
    }
  }

  @Test
  void testSubscribeLeaved(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      login(clientSdk, anotherAccountInfo);
      clientSdk.roomSdk().joinRoom(roomInfo.name());
      clientSdk.roomSdk().leaveRoom(roomInfo.id());
      login(clientSdk, anotherAccountInfo);
      clientSdk.messageSdk().subscribe(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: not a member");
    }
  }

  @Test
  void testSubscribeSuccessfully(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    final var roomId = clientSdk.messageSdk().subscribe(roomInfo.id());
    assertEquals(roomInfo.id(), roomId, "roomId: " + roomId);
  }

  @Test
  void testSubscribeRepeatedly(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    final var roomId1 = clientSdk.messageSdk().subscribe(roomInfo.id());
    final var roomId2 = clientSdk.messageSdk().subscribe(roomInfo.id());

    assertEquals(roomInfo.id(), roomId1, "roomId1: " + roomId1);
    assertEquals(roomInfo.id(), roomId2, "roomId2: " + roomId2);
  }
}
