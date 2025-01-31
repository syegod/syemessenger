package io.syemessenger.api.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.GetRoomsRequest;
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

public class JoinRoomIT {
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
  void testJoinRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    roomSdk.joinRoom(existingRoomInfo.name());
    final var roomsResponse = accountSdk.getRooms(new GetRoomsRequest());
    Assertions.assertNotNull(roomsResponse.roomInfos(), "roomInfos");
    assertEquals(1, roomsResponse.roomInfos().size());
    assertEquals(existingRoomInfo.id(), roomsResponse.roomInfos().getFirst().id());
    assertEquals(existingRoomInfo.name(), roomsResponse.roomInfos().getFirst().name());
    assertEquals(
        existingRoomInfo.description(), roomsResponse.roomInfos().getFirst().description());
    assertEquals(existingRoomInfo.owner(), roomsResponse.roomInfos().getFirst().owner());
    // TODO: fix time
    //    assertEquals(existingRoomInfo.createdAt(),
    // roomsResponse.roomInfos().getFirst().createdAt());
    //    assertEquals(existingRoomInfo.updatedAt(),
    // roomsResponse.roomInfos().getFirst().updatedAt());
  }

  @Test
  void testJoinRoomNotLoggedIn() {
    try {
      roomSdk.joinRoom(existingRoomInfo.name());
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(401, serviceException.errorCode());
      assertEquals("Not authenticated", serviceException.getMessage());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("testJoinRoomFailedMethodSource")
  void testJoinRoomFailed(String test, String name, int errorCode, String errorMessage) {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    try {
      roomSdk.joinRoom(name);
      fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode(), "errorCode");
      assertEquals(errorMessage, serviceException.getMessage(), "errorMessage");
    }
  }

  static Stream<Arguments> testJoinRoomFailedMethodSource() {
    return Stream.of(
        Arguments.of("Room name blank", "", 400, "Missing or invalid: name"),
        Arguments.of("Null room name", null, 400, "Missing or invalid: name"),
        Arguments.of("Wrong room name", randomAlphanumeric(20), 404, "Room not found"));
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
