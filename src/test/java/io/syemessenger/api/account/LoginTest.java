package io.syemessenger.api.account;

import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class LoginTest {

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

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testLoginFailedMethodSource")
  void testLoginFailed(
      String test, LoginAccountRequest request, int errorCode, String errorMessage) {
    try (final var accountSdk = new AccountSdkImpl(clientCodec)) {
      accountSdk.login(request);
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode());
      assertEquals(errorMessage, serviceException.getMessage());
    }
  }

  static Stream<Arguments> testLoginFailedMethodSource() {
    return Stream.of(
        Arguments.of(
            "Login to non-existing account",
            new LoginAccountRequest()
                .username(randomAlphanumeric(6, 30))
                .password(randomAlphanumeric(6, 25)),
            401,
            "Login failed"),
        Arguments.of(
            "Cannot provide both username and email",
            new LoginAccountRequest()
                .username(randomAlphanumeric(6, 30))
                .email(randomAlphanumeric(10, 50))
                .password(randomAlphanumeric(6, 25)),
            401,
            "Login failed"),
        Arguments.of(
            "Username and email are not provided",
            new LoginAccountRequest().password(randomAlphanumeric(6, 25)),
            401,
            "Login failed"),
        Arguments.of(
            "Password not provided",
            new LoginAccountRequest().username(randomAlphanumeric(6, 30)),
            401,
            "Login failed"),
        Arguments.of(
            "Invalid password",
            new LoginAccountRequest()
                .username(existingAccountInfo.username())
                .password(randomAlphanumeric(6, 25)),
            401,
            "Login failed"));
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
