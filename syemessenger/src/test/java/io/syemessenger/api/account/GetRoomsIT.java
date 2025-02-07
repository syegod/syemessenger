package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.assertRoom;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class GetRoomsIT {

  @Test
  void testGetRoomsNotLoggedIn(ClientSdk clientSdk) {
    try {
      clientSdk.accountSdk().getRooms(new GetRoomsRequest());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testGetRoomsFailedMethodSource")
  void testGetRoomsFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.accountSdk().getRooms(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, GetRoomsRequest request, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testGetRoomsFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Offset is negative",
            new GetRoomsRequest().offset(-50),
            400,
            "Missing or invalid: offset"),
        new FailedArgs(
            "Limit is negative",
            new GetRoomsRequest().limit(-50),
            400,
            "Missing or invalid: limit"),
        new FailedArgs(
            "Limit is over than max",
            new GetRoomsRequest().limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @Test
  void testGetRooms(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    final var rooms = clientSdk.accountSdk().getRooms(new GetRoomsRequest());
    assertEquals(1, rooms.totalCount());
    assertRoom(roomInfo, rooms.roomInfos().getFirst());
  }

  @Test
  void testGetRoomsEmpty(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    final var rooms = clientSdk.accountSdk().getRooms(new GetRoomsRequest());
    assertEquals(0, rooms.roomInfos().size());
  }
}
