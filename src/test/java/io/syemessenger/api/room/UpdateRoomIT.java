package io.syemessenger.api.room;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomNumeric;

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
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UpdateRoomIT {
  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;
  private static ClientSdk clientSdk;
  private static AccountSdk accountSdk;
  private static RoomSdk roomSdk;
  private static AccountInfo accountInfo;
  private static AccountInfo accountInfo2;
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
    accountInfo2 = createAccount();
    existingRoomInfo = createRoom(accountInfo);
  }

  @AfterEach
  void afterEach() {
    if (clientSdk != null) {
      clientSdk.close();
    }
  }

  @Test
  void testUpdateRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    final var description = randomAlphanumeric(20);
    final var roomInfo =
        roomSdk.updateRoom(
            new UpdateRoomRequest().roomId(existingRoomInfo.id()).description(description));

    assertEquals(existingRoomInfo.id(), roomInfo.id(), "roomInfo.id: " + roomInfo.id());
    assertEquals(accountInfo.username(), roomInfo.owner());
    assertEquals(description, roomInfo.description());
  }

  @Test
  void testUpdateRoomNotLoggedIn() {
    try {
      final var description = randomAlphanumeric(20);
      roomSdk.updateRoom(
          new UpdateRoomRequest().roomId(existingRoomInfo.id()).description(description));
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(401, serviceException.errorCode());
      assertEquals("Not authenticated", serviceException.getMessage());
    }
  }

  @Test
  void testUpdateRoomNotOwner() {
    try {
      accountSdk.login(
          new LoginAccountRequest().username(accountInfo2.username()).password("test12345"));
      roomSdk.updateRoom(
          new UpdateRoomRequest()
              .roomId(existingRoomInfo.id())
              .description(randomAlphanumeric(20)));
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(403, serviceException.errorCode());
      assertEquals("Not allowed", serviceException.getMessage());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testUpdateRoomFailedMethodSource")
  void testUpdateRoomFailed(
      String test, UpdateRoomRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
    try {
      roomSdk.updateRoom(request);
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode(), "errorCode");
      assertEquals(errorMessage, serviceException.getMessage(), "errorMessage");
    }
  }

  static Stream<Arguments> testUpdateRoomFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "No room id",
            new UpdateRoomRequest().description(randomAlphanumeric(20)),
            400,
            "Missing or invalid: roomId"),
        Arguments.of(
            "Wrong room id",
            new UpdateRoomRequest().roomId(100L).description(randomAlphanumeric(20)),
            404,
            "Room not found"),
        Arguments.of(
            "No description",
            new UpdateRoomRequest().roomId(existingRoomInfo.id()),
            400,
            "Missing or invalid: description"),
        Arguments.of(
            "Description too short",
            new UpdateRoomRequest()
                .roomId(existingRoomInfo.id())
                .description(randomAlphanumeric(5)),
            400,
            "Missing or invalid: description"),
        Arguments.of(
            "Description too long",
            new UpdateRoomRequest()
                .roomId(existingRoomInfo.id())
                .description(randomAlphanumeric(201)),
            400,
            "Missing or invalid: roomId")
    );
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
