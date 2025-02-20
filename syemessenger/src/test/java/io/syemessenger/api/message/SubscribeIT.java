package io.syemessenger.api.message;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.environment.AssertionUtils.awaitUntil;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceMessage;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.time.Duration;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class SubscribeIT {

  public static final Duration TIMEOUT = Duration.ofMillis(3000);

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
  void testSubscribe(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    final var cid = UUID.randomUUID();
  }

  @Test
  void testReceiveMessages(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    final var subscribe = clientSdk.messageSdk().subscribe(roomInfo.id());

    final var expected = "Test";
    clientSdk.messageSdk().send(expected);

    final ServiceMessage message =
        awaitUntil(
            () -> subscribe.poll(m -> m.qualifier().equals("v1/syemessenger/send")), TIMEOUT);

    assertEquals(expected, message.data());
  }
}
