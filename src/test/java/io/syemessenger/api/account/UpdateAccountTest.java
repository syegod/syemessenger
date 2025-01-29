package io.syemessenger.api.account;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
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
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

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
      final var username = randomAlphanumeric(8, 65);
      final var email = "example1@gmail.com";
      final var password = randomAlphanumeric(8, 65);

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

  @Test
  void testUpdateAccountNotLoggedIn() {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      final var username = randomAlphanumeric(8, 65);
      final var email = "example@gmail.com";

      sdk.updateAccount(new UpdateAccountRequest().username(username).email(email));
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(401, serviceException.errorCode());
      assertEquals("Not authenticated", serviceException.getMessage());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "failedUpdateAccountMethodSource")
  void testUpdateAccountFailed(
      String test, UpdateAccountRequest request, int errorCode, String errorMessage) {
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

  static Stream<Arguments> failedUpdateAccountMethodSource() {
    return Stream.of(
        Arguments.of(
            "Username too long",
            new UpdateAccountRequest().username(randomAlphanumeric(80)),
            400,
            "Invalid: username"),
        Arguments.of(
            "Username too short",
            new UpdateAccountRequest().username(randomAlphanumeric(7)),
            400,
            "Invalid: username"),
        Arguments.of(
            "Wrong email type",
            new UpdateAccountRequest().email(randomAlphanumeric(8, 65)),
            400,
            "Invalid: email"),
        Arguments.of(
            "Email too long",
            new UpdateAccountRequest().email(randomAlphanumeric(80)),
            400,
            "Invalid: email"),
        Arguments.of(
            "Email too short",
            new UpdateAccountRequest().email(randomAlphanumeric(7)),
            400,
            "Invalid: email"),
        Arguments.of(
            "Password too long",
            new UpdateAccountRequest().password(randomAlphanumeric(80)),
            400,
            "Invalid: password"),
        Arguments.of(
            "Password too short",
            new UpdateAccountRequest().password(randomAlphanumeric(7)),
            400,
            "Invalid: password"),
        Arguments.of(
            "Updating username to already existing",
            new UpdateAccountRequest().username(existingAccountInfo2.username()),
            400,
            "Cannot update account: already exists"),
        Arguments.of(
            "Updating email to already existing",
            new UpdateAccountRequest().email(existingAccountInfo2.email()),
            400,
            "Cannot update account: already exists"));
  }

  static AccountInfo createExistingAccount() {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      String username = randomAlphanumeric(8, 65);
      String email =
          randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
      String password = "test12345";

      return sdk.createAccount(
          new CreateAccountRequest().username(username).email(email).password(password));
    }
  }
}
