package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.room.CreateRoomRequest;
import io.syemessenger.api.room.RoomInfo;
import io.syemessenger.api.room.RoomSdk;
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

public class GetRoomsIT {
  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;
  private static ClientSdk clientSdk;
  private static AccountSdk accountSdk;
  private static RoomSdk roomSdk;
  private static AccountInfo existingAccountInfo;
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
    accountSdk = clientSdk.api(AccountSdk.class);
    roomSdk = clientSdk.api(RoomSdk.class);
    existingAccountInfo = createExistingAccount();
    existingRoomInfo = createRoom(existingAccountInfo);
  }

  @AfterEach
  void afterEach() {
    if (clientSdk != null) {
      clientSdk.close();
    }
  }

  @Test
  void testGetRooms() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    final var rooms = accountSdk.getRooms(new GetRoomsRequest());
    assertEquals(1, rooms.roomInfos().size());
    assertEquals(existingRoomInfo.id(), rooms.roomInfos().getFirst().id());
    assertEquals(existingRoomInfo.name(), rooms.roomInfos().getFirst().name());
    assertEquals(existingRoomInfo.description(), rooms.roomInfos().getFirst().description());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testGetRoomsFailedMethodSource")
  void testGetRoomsFailed(
      String test, GetRoomsRequest request, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    try {
      accountSdk.getRooms(request);
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode(), "errorCode");
      assertEquals(errorMessage, serviceException.getMessage(), "errorMessage");
    }
  }

  static Stream<Arguments> testGetRoomsFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Offset is negative",
            new GetRoomsRequest().offset(-50),
            400,
            "Missing or invalid: offset"),
        Arguments.of(
            "Limit is negative",
            new GetRoomsRequest().limit(-50),
            400,
            "Missing or invalid: limit"),
        Arguments.of(
            "Limit is over than max",
            new GetRoomsRequest().limit(60),
            400,
            "Missing or invalid: limit"));
  }

  @Test
  void testGetRoomsEmpty() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var rooms = accountSdk.getRooms(new GetRoomsRequest());
    assertEquals(0, rooms.roomInfos().size());
  }

  private static RoomInfo createRoom(AccountInfo accountInfo) {
    try (final var client = new ClientSdk(clientCodec)) {
      client
          .api(AccountSdk.class)
          .login(new LoginAccountRequest().username(accountInfo.username()).password("test12345"));
      return client
          .api(RoomSdk.class)
          .createRoom(new CreateRoomRequest().name(randomAlphanumeric(8, 65)));
    }
  }

  static AccountInfo createExistingAccount() {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      final var username = randomAlphanumeric(8, 65);
      final var email =
          randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
      final var password = "test12345";

      return clientSdk
          .api(AccountSdk.class)
          .createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));
    }
  }
}
