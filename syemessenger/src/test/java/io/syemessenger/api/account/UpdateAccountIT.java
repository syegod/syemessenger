package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class UpdateAccountIT {

  private static AccountInfo existingAccountInfo;
  private static AccountInfo anotherAccountInfo;

  @BeforeAll
  static void beforeAll() {
    existingAccountInfo = createAccount();
    anotherAccountInfo = createAccount();
  }

  @Test
  void testUpdateAccountNotLoggedIn() {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var api = clientSdk.api(AccountSdk.class);
      final var username = randomAlphanumeric(8, 65);
      final var email = "example@gmail.com";
      api.updateAccount(new UpdateAccountRequest().username(username).email(email));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "failedUpdateAccountMethodSource")
  void testUpdateAccountFailed(
      String test, UpdateAccountRequest request, int errorCode, String errorMessage) {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var api = clientSdk.api(AccountSdk.class);
      api.login(
          new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
      api.updateAccount(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> failedUpdateAccountMethodSource() {
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
            new UpdateAccountRequest().username(anotherAccountInfo.username()),
            400,
            "Cannot update account: already exists"),
        Arguments.of(
            "Updating email to already existing",
            new UpdateAccountRequest().email(anotherAccountInfo.email()),
            400,
            "Cannot update account: already exists"));
  }

  @Test
  void testUpdateAccount() {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var api = clientSdk.api(AccountSdk.class);
      final var username = randomAlphanumeric(8, 65);
      final var email = "example1@gmail.com";
      final var password = randomAlphanumeric(8, 65);

      final var account =
          api.createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));

      api.login(new LoginAccountRequest().username(account.username()).password(password));

      final var accountInfo =
          api.updateAccount(new UpdateAccountRequest().username(username).email(email));

      assertEquals(account.id(), accountInfo.id(), "accountInfo.id: " + accountInfo.id());
      assertEquals(username, accountInfo.username(), "username");
      assertEquals(email, accountInfo.email(), "email");
    }
  }
}
