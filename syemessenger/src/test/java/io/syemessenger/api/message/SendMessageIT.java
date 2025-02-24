package io.syemessenger.api.message;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.environment.AssertionUtils.awaitUntil;
import static io.syemessenger.environment.AssertionUtils.byQualifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.room.BlockMembersRequest;
import io.syemessenger.api.room.RemoveMembersRequest;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class SendMessageIT {

  public static final Duration TIMEOUT = Duration.ofMillis(3000);

  @Test
  void testSendMessageNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    try {
      clientSdk.messageSdk().subscribe(roomInfo.id());
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testSendMessageNotMember(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    try {
      clientSdk.messageSdk().subscribe(roomInfo.id());
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: not a member");
    }
  }

  @Test
  void testSendMessageBlocked(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);

    login(clientSdk, anotherAccountInfo);
    clientSdk.roomSdk().joinRoom(roomInfo.name());

    login(clientSdk, accountInfo);
    clientSdk
        .roomSdk()
        .blockRoomMembers(
            new BlockMembersRequest().memberIds(anotherAccountInfo.id()).roomId(roomInfo.id()));

    login(clientSdk, anotherAccountInfo);
    try {
      clientSdk.messageSdk().subscribe(roomInfo.id());
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: blocked");
    }
  }

  @Test
  void testSendMessageNonExistingRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.messageSdk().subscribe(Long.MAX_VALUE);
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 404, "Room not found");
    }
  }

  @Test
  void testSendMessageRemoved(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    clientSdk.roomSdk().joinRoom(roomInfo.name());
    login(clientSdk, accountInfo);
    clientSdk
        .roomSdk()
        .removeRoomMembers(
            new RemoveMembersRequest().memberIds(anotherAccountInfo.id()).roomId(roomInfo.id()));
    login(clientSdk, anotherAccountInfo);
    try {
      clientSdk.messageSdk().subscribe(roomInfo.id());
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: not a member");
    }
  }

  @Test
  void testSendMessageLeaved(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, anotherAccountInfo);
    clientSdk.roomSdk().joinRoom(roomInfo.name());
    clientSdk.roomSdk().leaveRoom(roomInfo.id());
    try {
      clientSdk.messageSdk().subscribe(roomInfo.id());
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Cannot subscribe: not a member");
    }
  }

  @Test
  void testSendMessageNotSubscribed(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.messageSdk().send("test");
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Not subscribed");
    }
  }

  @Test
  void sendMessageSuccessfully(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    clientSdk.messageSdk().subscribe(roomInfo.id());
    final var receiver = clientSdk.receiver();

    final var text = "Test message";
    final var roomId = clientSdk.messageSdk().send(text);
    assertEquals(roomInfo.id(), roomId, "roomId: " + roomId);

    final var message =
        (MessageInfo)
            awaitUntil(() -> receiver.poll(byQualifier("v1/syemessenger/messages")), TIMEOUT)
                .data();
    assertEquals(text, message.message());
  }

  @Test
  void testReceiveMultipleMessages(ClientSdk clientSdk, AccountInfo accountInfo) {
    int n = 25;
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    clientSdk.messageSdk().subscribe(roomInfo.id());
    final var receiver = clientSdk.receiver();

    for (var i = 0; i < n; i++) {
      final var text = "Test message" + i;
      final var roomId = clientSdk.messageSdk().send(text);
      assertEquals(roomInfo.id(), roomId, "roomId: " + roomId);

      final var message =
          (MessageInfo)
              awaitUntil(() -> receiver.poll(byQualifier("v1/syemessenger/messages")), TIMEOUT)
                  .data();
      assertEquals(text, message.message());
    }
  }
}
