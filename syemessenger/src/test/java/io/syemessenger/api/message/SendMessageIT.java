package io.syemessenger.api.message;

import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static io.syemessenger.environment.AssertionUtils.awaitUntil;
import static io.syemessenger.environment.AssertionUtils.byQualifier;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class SendMessageIT {

  public static final Duration TIMEOUT = Duration.ofMillis(3000);

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
        awaitUntil(() -> receiver.poll(byQualifier("v1/syemessenger/messages")), TIMEOUT);
    assertEquals(text, message.data());
  }
}
