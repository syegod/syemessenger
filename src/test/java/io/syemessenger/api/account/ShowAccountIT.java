package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
import io.syemessenger.api.ServiceException;
import io.syemessenger.environment.IntegrationEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ShowAccountIT {

  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;
  private static AccountInfo existingAccountInfo;

  @BeforeAll
  static void beforeAll() {
    environment = new IntegrationEnvironment();
    environment.start();

    existingAccountInfo = createExistingAccount();
  }

  @AfterAll
  static void afterAll() {
    if (environment != null) {
      environment.close();
    }
  }

  @Test
  void testShowAccount() {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      AccountSdk sdk = new AccountSdkImpl(clientSdk);
      sdk.login(
          new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
      final var publicAccountInfo = sdk.showAccount(existingAccountInfo.id());
      Assertions.assertEquals(existingAccountInfo.username(), publicAccountInfo.username());
    }
  }

  @Test
  void testShowAccountNotLoggedIn() {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      AccountSdk sdk = new AccountSdkImpl(clientSdk);
      sdk.showAccount(existingAccountInfo.id());
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
      AccountSdk sdk = new AccountSdkImpl(clientSdk);
      String username = randomAlphanumeric(8, 65);
      String email = "test@gmail.com";
      String password = "test12345";

      return sdk.createAccount(
          new CreateAccountRequest().username(username).email(email).password(password));
    }
  }
}
