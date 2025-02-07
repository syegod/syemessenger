package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.assertRoom;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
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
      clientSdk.roomSdk().getRoom(roomInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testGetRoomFailedMethodSource")
  void testGetRoomFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.roomSdk().getRoom(args.id);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(String test, Long id, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testGetRoomFailedMethodSource() {
    return Stream.of(
        new FailedArgs("Null id", null, 400, "Missing or invalid: id"),
        new FailedArgs("Non-existing id", Long.MAX_VALUE, 404, "Room not found"));
  }

  @Test
  void testGetRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var existingRoomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    final var roomInfo = clientSdk.roomSdk().getRoom(existingRoomInfo.id());
    assertRoom(existingRoomInfo, roomInfo);
  }
}
