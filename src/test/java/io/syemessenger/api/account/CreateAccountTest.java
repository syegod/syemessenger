package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.syemessenger.ServiceBootstrap;
import io.syemessenger.ServiceConfig;
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

public class CreateAccountTest {

  private static final ClientCodec clientCodec = ClientCodec.getInstance();
  private static IntegrationEnvironment environment;

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

  @Test
  void testCreateAccount() {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
      String username = "Test123";
      String email = "example@gmail.com";
      String password = "test123";

      final AccountInfo accountInfo =
          sdk.createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));
      assertEquals(username, accountInfo.username());
      assertEquals(email, accountInfo.email());
      assertEquals(AccountStatus.NON_CONFIRMED, accountInfo.status());
      assertNotNull(accountInfo.createdAt());
      assertTrue(accountInfo.id() > 0, "accountInfo.id: " + accountInfo.id());
    }
  }

  @MethodSource(value = "failedAccountMethodSource")
  @ParameterizedTest
  void testCreateAccountFailed(CreateAccountRequest request, int errorCode, String errorMessage) {
    try (AccountSdk sdk = new AccountSdkImpl(clientCodec)) {
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
            new CreateAccountRequest().username("").email("").password(""),
            400,
            "Missing or invalid: username"),
        Arguments.of(new CreateAccountRequest(), 400, "Missing or invalid: username"),
        Arguments.of(
            new CreateAccountRequest().username("test123").email("test@gmail.com"),
            400,
            "Missing or invalid: password"),
        Arguments.of(
            new CreateAccountRequest().email("test@gmail.com").password("test123"),
            400,
            "Missing or invalid: username"),
        Arguments.of(
            new CreateAccountRequest().username("test321321").password("test123"),
            400,
            "Missing or invalid: email"),

        // Username too short
        Arguments.of(
            new CreateAccountRequest()
                .username("usr1")
                .email("testuser@gmail.com")
                .password("password123"),
            400,
            "Missing or invalid: username"),

        // Username too long
        Arguments.of(
            new CreateAccountRequest()
                .username("aVeryLongUsernameExceedingThirtyCharacters")
                .email("testuser@gmail.com")
                .password("password123"),
            400,
            "Missing or invalid: username"),

        // Email too short
        Arguments.of(
            new CreateAccountRequest().username("validuser").email("t@e.c").password("password123"),
            400,
            "Missing or invalid: email"),

        // Email too long
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("averylongemailaddresswhichexceedsthefiftycharacterslimit@example.com")
                .password("password123"),
            400,
            "Missing or invalid: email"),

        // Password too short
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("testuser@gmail.com")
                .password("pass"),
            400,
            "Missing or invalid: password"),

        // Password too long
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("testuser@gmail.com")
                .password("averylongpasswordexceedingthetwentyfivecharacterslimit"),
            400,
            "Missing or invalid: password")

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
}
