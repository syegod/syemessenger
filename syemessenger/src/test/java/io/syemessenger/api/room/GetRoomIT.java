package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.room.RoomAssertions.assertRoom;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class GetRoomIT {

  @Test
  void testGetRoomNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      clientSdk.api(RoomSdk.class).getRoom(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomFailedMethodSource")
  void testGetRoomFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    clientSdk
        .api(AccountSdk.class)
        .login(new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      clientSdk.api(RoomSdk.class).getRoom(args.id);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(String test, Long id, int errorCode, String errorMessage) {}

  private static Stream<?> testGetRoomFailedMethodSource() {
    return Stream.of(
        new FailedArgs("Null id", null, 400, "Missing or invalid: id"),
        new FailedArgs("Non-existing id", Long.MAX_VALUE, 404, "Room not found"));
  }

  @Test
  void testGetRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var existingRoomInfo = createRoom(accountInfo);
    clientSdk
        .api(AccountSdk.class)
        .login(new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    final var roomInfo = clientSdk.api(RoomSdk.class).getRoom(existingRoomInfo.id());
    assertRoom(existingRoomInfo, roomInfo);
  }
}
