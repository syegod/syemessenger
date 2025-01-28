package io.syemessenger.api.account;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomNumeric;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ServiceException;
import io.syemessenger.environment.IntegrationEnvironment;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class UpdateAccountTest {

  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;
  private static AccountInfo existingAccountInfo;
  private static AccountInfo existingAccountInfo2;

  @BeforeAll
  static void beforeAll() {
    environment = new IntegrationEnvironment();
    environment.start();

    existingAccountInfo = createExistingAccount();
    existingAccountInfo2 = createExistingAccount();
  }

  @AfterAll
  static void afterAll() {
    if (environment != null) {
      environment.close();
    }
  }

  @Test
  void testUpdateAccount() {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      final var username = randomAlphanumeric(6, 30);
      final var email = randomAlphanumeric(10, 50);
      final var password = randomAlphanumeric(6, 25);

      final var account =
          sdk.createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));

      sdk.login(new LoginAccountRequest().username(account.username()).password(password));

      final var accountInfo =
          sdk.updateAccount(new UpdateAccountRequest().username(username).email(email));

      assertEquals(account.id(), accountInfo.id(), "accountInfo.id: " + accountInfo.id());
      assertEquals(username, accountInfo.username());
      assertEquals(email, accountInfo.email());
    }
  }

  @ParameterizedTest
  @MethodSource(value = "failedUpdateAccountMethodSource")
  void testUpdateAccountFailed(UpdateAccountRequest request, int errorCode, String errorMessage) {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      sdk.login(
          new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
      sdk.updateAccount(request);
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode());
      assertEquals(errorMessage, serviceException.getMessage());
    }
  }

  // TODO: create tests for authorization
  static Stream<Arguments> failedUpdateAccountMethodSource() {
    return Stream.of(
        // Length checks
        Arguments.of(
            new UpdateAccountRequest().username(randomAlphanumeric(35)), 400, "Invalid: username"),
        Arguments.of(
            new UpdateAccountRequest().username(randomAlphanumeric(5)), 400, "Invalid: username"),
        Arguments.of(
            new UpdateAccountRequest().email(randomAlphanumeric(55)), 400, "Invalid: email"),
        Arguments.of(
            new UpdateAccountRequest().email(randomAlphanumeric(5)), 400, "Invalid: email"),
        Arguments.of(
            new UpdateAccountRequest().password(randomAlphanumeric(35)), 400, "Invalid: password"),
        Arguments.of(
            new UpdateAccountRequest().password(randomAlphanumeric(5)), 400, "Invalid: password"),
        // Updating username to already existing
        Arguments.of(
            new UpdateAccountRequest().username(existingAccountInfo2.username()),
            400,
            "Cannot update account: already exists"),
        // Updating email to already existing
        Arguments.of(
            new UpdateAccountRequest().email(existingAccountInfo2.email()),
            400,
            "Cannot update account: already exists"));
  }

  static AccountInfo createExistingAccount() {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      String username = randomAlphanumeric(6, 30);
      String email = randomAlphanumeric(10, 50);
      String password = "test12345";

      return sdk.createAccount(
          new CreateAccountRequest().username(username).email(email).password(password));
    }
  }
}
