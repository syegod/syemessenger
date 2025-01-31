package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceException;
import io.syemessenger.environment.IntegrationEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetAccountIT {

  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;
  private static ClientSdk clientSdk;
  private static AccountSdk accountSdk;
  private static AccountInfo existingAccountInfo;
  private static AccountInfo existingAccountInfo2;

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
    existingAccountInfo = createExistingAccount();
    existingAccountInfo2 = createExistingAccount();
  }

  @Test
  void testGetAccount() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var accountInfo = accountSdk.getAccount(existingAccountInfo2.id());
    assertEquals(existingAccountInfo2.id(), accountInfo.id());
    assertEquals(existingAccountInfo2.username(), accountInfo.username());
    assertEquals(existingAccountInfo2.email(), accountInfo.email());
  }

  @Test
  void testGetAccountOwn() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var accountInfo = accountSdk.getAccount(null);
    assertEquals(existingAccountInfo.id(), accountInfo.id());
    assertEquals(existingAccountInfo.username(), accountInfo.username());
    assertEquals(existingAccountInfo.email(), accountInfo.email());
  }

  @Test
  void testGetAccountNotLoggedIn() {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      final var api = clientSdk.api(AccountSdk.class);
      api.getAccount(existingAccountInfo.id());
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(401, serviceException.errorCode());
      assertEquals("Not authenticated", serviceException.getMessage());
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
