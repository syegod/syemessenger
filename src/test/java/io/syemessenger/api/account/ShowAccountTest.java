package io.syemessenger.api.account;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.account.repository.Account;
import io.syemessenger.environment.IntegrationEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ShowAccountTest {

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
    try(AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      final var publicAccountInfo = sdk.showAccount(existingAccountInfo.id());
      Assertions.assertEquals(existingAccountInfo.username(), publicAccountInfo.username());
    }
  }

  static AccountInfo createExistingAccount() {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      String username = randomAlphanumeric(6, 30);
      String email = randomAlphanumeric(10, 50);
      String password = randomAlphanumeric(6, 25);

      return sdk.createAccount(
          new CreateAccountRequest().username(username).email(email).password(password));
    }
  }
}
