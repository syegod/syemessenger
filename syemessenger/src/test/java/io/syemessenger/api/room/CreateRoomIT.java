package io.syemessenger.api.room;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.room.RoomAssertions.createRoom;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class CreateRoomIT {

  private static AccountInfo accountInfo;
  private static RoomInfo existingRoomInfo;

  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private RoomSdk roomSdk;

  @BeforeAll
  static void beforeAll() {
    accountInfo = createAccount();
    existingRoomInfo = createRoom(accountInfo);
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);
  }

  @AfterEach
  void afterEach() {
    CloseHelper.close(clientSdk);
  }

  @Test
  void testCreateRoomNotLoggedIn() {
    try {
      final var name = randomAlphanumeric(20);
      final var description = randomAlphanumeric(20);
      roomSdk.createRoom(new CreateRoomRequest().name(name).description(description));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testCreateRoomNoDescription() {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var name = randomAlphanumeric(20);
    final var roomInfo = roomSdk.createRoom(new CreateRoomRequest().name(name));

    assertTrue(roomInfo.id() > 0, "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo.username(), roomInfo.owner());
    assertNull(roomInfo.description(), "roomInfo.description: " + roomInfo.description());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testCreateRoomFailedMethodSource")
  void testCreateRoomFailed(
      String test, CreateRoomRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      roomSdk.createRoom(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testCreateRoomFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Room name too short",
            new CreateRoomRequest().name(randomAlphanumeric(7)).description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: name"),
        Arguments.of(
            "Room name too long",
            new CreateRoomRequest()
                .name(randomAlphanumeric(65))
                .description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: name"),
        Arguments.of(
            "No room name",
            new CreateRoomRequest().description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: name"),
        Arguments.of(
            "Room description too long",
            new CreateRoomRequest()
                .name(randomAlphanumeric(8, 65))
                .description(randomAlphanumeric(201)),
            400,
            "Missing or invalid: description"),
        Arguments.of(
            "Room name already exists",
            new CreateRoomRequest().name(existingRoomInfo.name()),
            400,
            "Cannot create room: already exists"));
  }

  @Test
  void testCreateRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var name = randomAlphanumeric(20);
    final var description = randomAlphanumeric(20);
    final var roomInfo =
        roomSdk.createRoom(new CreateRoomRequest().name(name).description(description));

    assertTrue(roomInfo.id() > 0, "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo.username(), roomInfo.owner());
    assertEquals(description, roomInfo.description());
  }
}
