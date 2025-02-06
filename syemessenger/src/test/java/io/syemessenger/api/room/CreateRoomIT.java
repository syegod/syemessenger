package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.login;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class CreateRoomIT {

  private static RoomInfo existingRoomInfo;

  @BeforeAll
  static void beforeAll(AccountInfo accountInfo) {
    existingRoomInfo = createRoom(accountInfo);
  }

  @Test
  void testCreateRoomNotLoggedIn(ClientSdk clientSdk) {
    try {
      final var name = randomAlphanumeric(20);
      final var description = randomAlphanumeric(20);
      clientSdk
          .roomSdk()
          .createRoom(new CreateRoomRequest().name(name).description(description));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testCreateRoomNoDescription(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);

    final var name = randomAlphanumeric(20);
    final var roomInfo =
        clientSdk.roomSdk().createRoom(new CreateRoomRequest().name(name));

    assertTrue(roomInfo.id() > 0, "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo.username(), roomInfo.owner());
    assertNull(roomInfo.description(), "roomInfo.description: " + roomInfo.description());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testCreateRoomFailedMethodSource")
  void testCreateRoomFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    try {
      clientSdk.roomSdk().createRoom(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, CreateRoomRequest request, int errorCode, String errorMessage) {}

  private static Stream<?> testCreateRoomFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Room name too short",
            new CreateRoomRequest().name(randomAlphanumeric(7)).description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: name"),
        new FailedArgs(
            "Room name too long",
            new CreateRoomRequest()
                .name(randomAlphanumeric(65))
                .description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: name"),
        new FailedArgs(
            "No room name",
            new CreateRoomRequest().description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: name"),
        new FailedArgs(
            "Room description too long",
            new CreateRoomRequest()
                .name(randomAlphanumeric(8, 65))
                .description(randomAlphanumeric(201)),
            400,
            "Missing or invalid: description"),
        new FailedArgs(
            "Room name already exists",
            new CreateRoomRequest().name(existingRoomInfo.name()),
            400,
            "Cannot create room: already exists"));
  }

  @Test
  void testCreateRoom(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);

    final var name = randomAlphanumeric(20);
    final var description = randomAlphanumeric(20);
    final var roomInfo =
        clientSdk
            .roomSdk()
            .createRoom(new CreateRoomRequest().name(name).description(description));

    assertTrue(roomInfo.id() > 0, "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo.username(), roomInfo.owner());
    assertEquals(description, roomInfo.description());
  }
}
