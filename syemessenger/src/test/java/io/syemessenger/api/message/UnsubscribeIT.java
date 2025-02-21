package io.syemessenger.api.message;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.environment.AssertionUtils.awaitUntil;
import static io.syemessenger.environment.AssertionUtils.byQualifier;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class UnsubscribeIT {

  public static final Duration TIMEOUT = Duration.ofMillis(3000);

  @Test
  void testUnsubscribeNotSubscribed(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.messageSdk().unsubscribe();
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Not subscribed");
    }
  }

  @Test
  void testUnsubscribeRepeatedly(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    clientSdk.messageSdk().subscribe(roomInfo.id());
    clientSdk.messageSdk().unsubscribe();
    try {
      clientSdk.messageSdk().unsubscribe();
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 400, "Not subscribed");
    }
  }

  @Test
  void testUnsubscribeSuccessfully(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    clientSdk.messageSdk().subscribe(roomInfo.id());
    final var roomId = clientSdk.messageSdk().unsubscribe();
    assertEquals(roomInfo.id(), roomId);
  }

  @Test
  void testStopReceivingMessages(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    clientSdk.messageSdk().subscribe(roomInfo.id());
    final var receiver = clientSdk.receiver();

    clientSdk.messageSdk().send("test");
    awaitUntil(() -> receiver.poll(byQualifier("v1/syemessenger/messages")), TIMEOUT);

    clientSdk.messageSdk().unsubscribe();

    try {
      awaitUntil(() -> receiver.poll(byQualifier("v1/syemessenger/messages")), TIMEOUT);
      fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(RuntimeException.class, ex);
      assertEquals("Timeout", ex.getMessage());
    }
  }
}
