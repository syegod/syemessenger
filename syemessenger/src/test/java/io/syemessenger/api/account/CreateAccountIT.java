package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
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
public class CreateAccountIT {

  private static AccountInfo existingAccountInfo;

  @BeforeAll
  static void beforeAll() {
    existingAccountInfo = createAccount();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "failedAccountMethodSource")
  void testCreateAccountFailed(
      String test, CreateAccountRequest request, int errorCode, String errorMessage) {
    try (ClientSdk clientSdk = new ClientSdk()) {
      clientSdk.api(AccountSdk.class).createAccount(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> failedAccountMethodSource() {
    return Stream.of(
        Arguments.of(
            "All parameters are empty strings",
            new CreateAccountRequest().username("").email("").password(""),
            400,
            "Missing or invalid: username"),
        Arguments.of(
            "All parameters are null",
            new CreateAccountRequest(),
            400,
            "Missing or invalid: username"),
        Arguments.of(
            "No password",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email("example@email.com"),
            400,
            "Missing or invalid: password"),
        Arguments.of(
            "No username",
            new CreateAccountRequest()
                .email("example@email.com")
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: username"),
        Arguments.of(
            "No email",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: email"),
        Arguments.of(
            "Username too short",
            new CreateAccountRequest()
                .username(randomAlphanumeric(7))
                .email(randomAlphanumeric(8, 65))
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: username"),
        Arguments.of(
            "Username too long",
            new CreateAccountRequest()
                .username(randomAlphanumeric(80))
                .email(randomAlphanumeric(8, 65))
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: username"),
        Arguments.of(
            "Wrong email type",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email(randomAlphanumeric(8, 65))
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: email"),
        Arguments.of(
            "Email too short",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email(randomAlphanumeric(7))
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: email"),
        Arguments.of(
            "Email too long",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email(randomAlphanumeric(80))
                .password(randomAlphanumeric(8, 65)),
            400,
            "Missing or invalid: email"),
        Arguments.of(
            "Password too short",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email("example@email.com")
                .password(randomAlphanumeric(7)),
            400,
            "Missing or invalid: password"),
        Arguments.of(
            "Password too long",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email("example@email.com")
                .password(randomAlphanumeric(80)),
            400,
            "Missing or invalid: password"),
        Arguments.of(
            "Username already exists",
            new CreateAccountRequest()
                .username(existingAccountInfo.username())
                .email("example@email.com")
                .password(randomAlphanumeric(8, 65)),
            400,
            "Cannot create account: already exists"),
        Arguments.of(
            "Email already exists",
            new CreateAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email(existingAccountInfo.email())
                .password(randomAlphanumeric(8, 65)),
            400,
            "Cannot create account: already exists"));
  }

  @Test
  void testCreateAccount() {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var api = clientSdk.api(AccountSdk.class);
      String username = randomAlphanumeric(8, 65);
      String email =
          randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
      String password = randomAlphanumeric(8, 65);

      final AccountInfo accountInfo =
          api.createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));
      assertEquals(username, accountInfo.username(), "username");
      assertEquals(email, accountInfo.email(), "email");
      assertNotNull(accountInfo.createdAt(), "createdAt");
      assertTrue(accountInfo.id() > 0, "accountInfo.id: " + accountInfo.id());
    }
  }
}
