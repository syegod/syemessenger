package io.syemessenger.api.room;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.AccountInfo;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.CreateAccountRequest;
import io.syemessenger.api.account.LoginAccountRequest;
import io.syemessenger.environment.IntegrationEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetRoomIT {
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

  @Test
  void testGetRoom() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var roomInfo = roomSdk.getRoom(existingRoomInfo.id());

    assertEquals(existingRoomInfo.id(), roomInfo.id());
    assertEquals(existingRoomInfo.name(), roomInfo.name());
    assertEquals(existingRoomInfo.description(), roomInfo.description());
  }

  @Test
  void testGetRoomNotLoggedIn() {
    try {
      roomSdk.getRoom(existingRoomInfo.id());
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(401, serviceException.errorCode());
      assertEquals("Not authenticated", serviceException.getMessage());
    }
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
