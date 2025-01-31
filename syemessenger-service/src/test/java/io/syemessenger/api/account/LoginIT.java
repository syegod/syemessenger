package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironment;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoginIT {

  private static IntegrationEnvironment environment;
  private static AccountInfo existingAccountInfo;

  @BeforeAll
  static void beforeAll() {
    environment = new IntegrationEnvironment();
    environment.start();

    existingAccountInfo = createAccount();
  }

  @AfterAll
  static void afterAll() {
    CloseHelper.close(environment);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testLoginFailedMethodSource")
  void testLoginFailed(
      String test, LoginAccountRequest request, int errorCode, String errorMessage) {
    try (ClientSdk clientSdk = new ClientSdk()) {
      clientSdk.api(AccountSdk.class).login(request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, errorCode, errorMessage);
    }
  }

  private static Stream<Arguments> testLoginFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Login to non-existing account",
            new LoginAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        Arguments.of(
            "Cannot provide both username and email",
            new LoginAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email("example@gmail.com")
                .password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        Arguments.of(
            "Username and email are not provided",
            new LoginAccountRequest().password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        Arguments.of(
            "Password not provided",
            new LoginAccountRequest().username(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        Arguments.of(
            "Invalid password",
            new LoginAccountRequest()
                .username(existingAccountInfo.username())
                .password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"));
  }

  @Test
  void testLoginByUsername() {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var accountId =
          clientSdk
              .api(AccountSdk.class)
              .login(
                  new LoginAccountRequest()
                      .username(existingAccountInfo.username())
                      .password("test12345"));

      assertEquals(existingAccountInfo.id(), accountId, "accountId");
    }
  }

  @Test
  void testLoginByEmail() {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var accountId =
          clientSdk
              .api(AccountSdk.class)
              .login(
                  new LoginAccountRequest()
                      .email(existingAccountInfo.email())
                      .password("test12345"));

      assertEquals(existingAccountInfo.id(), accountId, "accountId");
    }
  }
}
