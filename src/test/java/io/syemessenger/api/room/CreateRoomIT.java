package io.syemessenger.api.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.AccountSdkImpl;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.IntegrationEnvironment;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CreateRoomIT {

  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;
  private static ClientSdk clientSdk;
  private static AccountSdk accountSdk;
  private static RoomSdk roomSdk;
  private static AccountInfo accountInfo;
  private static RoomInfo existingRoomInfo;

  @BeforeAll
  static void beforeAll() {
    environment = new IntegrationEnvironment();
    environment.start();
  }

  @AfterAll
  static void afterAll() {
    if (environment != null) {
      environment.close();
    }
  }

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk(clientCodec);
    accountSdk = new AccountSdkImpl(clientSdk);
    roomSdk = new RoomSdkImpl(clientSdk);
    accountInfo = createAccount();
    existingRoomInfo = createRoom(accountInfo);
  }

  @AfterEach
  void afterEach() {
    if (clientSdk != null) {
      clientSdk.close();
    }
  }

  @Test
  void testCreateRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var name = randomAlphanumeric(20);
    final var description = randomAlphanumeric(20);
    final var roomInfo =
        roomSdk.createRoom(new CreateRoomRequest().name(name).description(description));

    assertTrue(roomInfo.id() > 0, "roomInfo: " + roomInfo);
    assertEquals(accountInfo.username(), roomInfo.owner());
    assertEquals(description, roomInfo.description());
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

  @Test
  void testCreateRoomNotLoggedIn() {
    try {
      final var name = randomAlphanumeric(20);
      final var description = randomAlphanumeric(20);
      roomSdk.createRoom(new CreateRoomRequest().name(name).description(description));
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(401, serviceException.errorCode());
      assertEquals("Not authenticated", serviceException.getMessage());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testCreateRoomFailedMethodSource")
  void testCreateRoomFailed(
      String test, CreateRoomRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      roomSdk.createRoom(request);
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode(), "errorCode");
      assertEquals(errorMessage, serviceException.getMessage(), "errorMessage");
    }
  }

  static Stream<Arguments> testCreateRoomFailedMethodSource() {
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

  private static RoomInfo createRoom(AccountInfo accountInfo) {
    try (final var client = new ClientSdk(clientCodec)) {
      final var accountApi = new AccountSdkImpl(client);
      accountApi.login(
          new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
      final var roomApi = new RoomSdkImpl(client);
      return roomApi.createRoom(new CreateRoomRequest().name(randomAlphanumeric(8, 65)));
    }
  }

  private static AccountInfo createAccount() {
    String username = randomAlphanumeric(8, 65);
    String email =
        randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
    String password = "test12345";

    return accountSdk.createAccount(
        new CreateAccountRequest().username(username).email(email).password(password));
  }
}
