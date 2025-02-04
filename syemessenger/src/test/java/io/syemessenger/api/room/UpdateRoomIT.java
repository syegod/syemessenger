package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class UpdateRoomIT {

  @Test
  void testUpdateRoomNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);
      final var description = randomAlphanumeric(20);
      clientSdk
          .roomSdk()
          .updateRoom(new UpdateRoomRequest().roomId(roomInfo.id()).description(description));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testUpdateRoomNotOwner(
      ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    try {
      final var roomInfo = createRoom(accountInfo);

      login(clientSdk, anotherAccountInfo);

      clientSdk
          .roomSdk()
          .updateRoom(
              new UpdateRoomRequest().roomId(roomInfo.id()).description(randomAlphanumeric(20)));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 403, "Not allowed");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testUpdateRoomFailedMethodSource")
  void testUpdateRoomFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    final var roomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);
    try {
      clientSdk.roomSdk().updateRoom(args.request.apply(roomInfo));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test,
      Function<RoomInfo, UpdateRoomRequest> request,
      int errorCode,
      String errorMessage) {}

  private static Stream<?> testUpdateRoomFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "No room id",
            roomInfo -> new UpdateRoomRequest().description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: roomId"),
        new FailedArgs(
            "Wrong room id",
            roomInfo -> new UpdateRoomRequest().roomId(100L).description(randomAlphanumeric(20)),
            404,
            "Room not found"),
        new FailedArgs(
            "No description",
            roomInfo -> new UpdateRoomRequest().roomId(roomInfo.id()),
            400,
            "Missing or invalid: description"),
        new FailedArgs(
            "Description too short",
            roomInfo ->
                new UpdateRoomRequest().roomId(roomInfo.id()).description(randomAlphanumeric(5)),
            400,
            "Missing or invalid: description"),
        new FailedArgs(
            "Description too long",
            roomInfo ->
                new UpdateRoomRequest().roomId(roomInfo.id()).description(randomAlphanumeric(201)),
            400,
            "Missing or invalid: description"));
  }

  @Test
  void testUpdateRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var origRoomInfo = createRoom(accountInfo);
    login(clientSdk, accountInfo);

    final var description = randomAlphanumeric(20);
    final var roomInfo =
        clientSdk
            .roomSdk()
            .updateRoom(new UpdateRoomRequest().roomId(origRoomInfo.id()).description(description));

    assertEquals(origRoomInfo.id(), roomInfo.id(), "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo.username(), roomInfo.owner(), "roomInfo.owner");
    assertEquals(description, roomInfo.description(), "roomInfo.description");
  }
}
