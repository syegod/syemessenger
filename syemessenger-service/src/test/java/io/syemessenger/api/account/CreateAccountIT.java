package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientCodec;
import io.syemessenger.api.ClientSdk;
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

public class CreateAccountIT {

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
  void testCreateAccount() {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      AccountSdk sdk = new AccountSdkImpl(clientSdk);
      String username = randomAlphanumeric(8, 65);
      String email =
          randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
      String password = randomAlphanumeric(8, 65);

      final AccountInfo accountInfo =
          sdk.createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));
      assertEquals(username, accountInfo.username());
      assertEquals(email, accountInfo.email());
      assertNotNull(accountInfo.createdAt());
      assertTrue(accountInfo.id() > 0, "accountInfo.id: " + accountInfo.id());
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "failedAccountMethodSource")
  void testCreateAccountFailed(
      String test, CreateAccountRequest request, int errorCode, String errorMessage) {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      AccountSdk sdk = new AccountSdkImpl(clientSdk);
      sdk.createAccount(request);
      Assertions.fail("Expected exception");
    } catch (Exception ex) {
      assertInstanceOf(ServiceException.class, ex, "Exception: " + ex);
      final var serviceException = (ServiceException) ex;
      assertEquals(errorCode, serviceException.errorCode());
      assertEquals(errorMessage, serviceException.getMessage());
    }
  }

  static Stream<Arguments> failedAccountMethodSource() {
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
            "Cannot create account: already exists")

        //        TODO: Move to successful scenarios
        //        // Boundary test: Username at minimum length
        //        Arguments.of(
        //            new CreateAccountRequest()
        //                .username("usrnam")
        //                .email("testuser@gmail.com")
        //                .password("password123"),
        //            400,
        //            "Invalid credentials"), // Assuming other invalid criteria
        //
        //        // Boundary test: Username at maximum length
        //        Arguments.of(
        //            new CreateAccountRequest()
        //                .username("thirtycharusernameexactlyyayy")
        //                .email("testuser@gmail.com")
        //                .password("password123"),
        //            400,
        //            "Invalid credentials"), // Assuming other invalid criteria
        //
        //        // Boundary test: Email at minimum length
        //        Arguments.of(
        //            new CreateAccountRequest()
        //                .username("validuser")
        //                .email("test@domain.com")
        //                .password("password123"),
        //            400,
        //            "Invalid credentials"), // Assuming other invalid criteria
        //
        //        // Boundary test: Email at maximum length
        //        Arguments.of(
        //            new CreateAccountRequest()
        //                .username("validuser")
        //                .email("averylongemailaddresswithmaximum50charactrs@ex.com")
        //                .password("password123"),
        //            400,
        //            "Invalid credentials"), // Assuming other invalid criteria
        //
        //        // Boundary test: Password at minimum length
        //        Arguments.of(
        //            new CreateAccountRequest()
        //                .username("validuser")
        //                .email("testuser@gmail.com")
        //                .password("passw6"),
        //            400,
        //            "Invalid credentials"), // Assuming other invalid criteria
        //
        //        // Boundary test: Password at maximum length
        //        Arguments.of(
        //            new CreateAccountRequest()
        //                .username("validuser")
        //                .email("testuser@gmail.com")
        //                .password("thisis25characterslongxx"),
        //            400,
        //            "Invalid credentials") // Assuming other invalid criteria
        );
  }

  static AccountInfo createExistingAccount() {
    try (ClientSdk clientSdk = new ClientSdk(clientCodec)) {
      AccountSdk sdk = new AccountSdkImpl(clientSdk);
      String username = randomAlphanumeric(8, 65);
      String email =
          randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
      String password = randomAlphanumeric(8, 65);

      return sdk.createAccount(
          new CreateAccountRequest().username(username).email(email).password(password));
    }
  }
}
