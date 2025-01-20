package io.syemessenger.api.account;

import io.syemessenger.WebSocketServer;
import io.syemessenger.api.ServiceException;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CreateAccountTest {

  private static WebSocketServer server;

  @BeforeAll
  static void beforeAll() {
    server = WebSocketServer.start(8080);
  }

  @AfterAll
  static void afterAll() {
    if (server != null) {
      server.close();
    }
  }

  @Test
  void testCreateAccount() {
    try (AccountSdk sdk = new AccountSdkImpl()) {
      String username = "Test";
      String email = "example@gmail.com";
      String password = "test123";

      final Long accountId =
          sdk.createAccount(
              new CreateAccountRequest().username(username).email(email).password(password));

      Assertions.assertTrue(accountId > 0);

      final var publicAccountInfo = sdk.showAccount(accountId);
      Assertions.assertEquals(accountId, publicAccountInfo.id());
      Assertions.assertEquals(email, publicAccountInfo.username());
    }
  }

  @MethodSource(value = "failedAccountMethodSource")
  @ParameterizedTest
  void testCreateAccountFailed(CreateAccountRequest request, int errorCode, String errorMessage) {
    try (AccountSdk sdk = new AccountSdkImpl()) {
      sdk.createAccount(request);
      Assertions.fail("Expected exception");
    } catch (ServiceException ex) {
      Assertions.assertEquals(errorCode, ex.errorCode());
      Assertions.assertEquals(errorMessage, ex.getMessage());
    }
  }

  static Stream<Arguments> failedAccountMethodSource() {
    return Stream.of(
        Arguments.of(
            new CreateAccountRequest().username("").email("").password(""),
            400,
            "Invalid credentials"),
        Arguments.of(new CreateAccountRequest(), 400, "Invalid credentials"),
        Arguments.of(
            new CreateAccountRequest().username("test").email("test@gmail.com"),
            400,
            "Invalid credentials"),
        Arguments.of(
            new CreateAccountRequest().email("test@gmail.com").password("test123"),
            400,
            "Invalid credentials"),
        Arguments.of(
            new CreateAccountRequest().username("test321321").password("test123"),
            400,
            "Invalid credentials"),

        // Username too short
        Arguments.of(
            new CreateAccountRequest()
                .username("usr1")
                .email("testuser@gmail.com")
                .password("password123"),
            400,
            "Username must be between 6 and 30 characters"),

        // Username too long
        Arguments.of(
            new CreateAccountRequest()
                .username("aVeryLongUsernameExceedingThirtyCharacters")
                .email("testuser@gmail.com")
                .password("password123"),
            400,
            "Username must be between 6 and 30 characters"),

        // Email too short
        Arguments.of(
            new CreateAccountRequest().username("validuser").email("t@e.c").password("password123"),
            400,
            "Email must be between 10 and 50 characters"),

        // Email too long
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("averylongemailaddresswhichexceedsthefiftycharacterslimit@example.com")
                .password("password123"),
            400,
            "Email must be between 10 and 50 characters"),

        // Password too short
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("testuser@gmail.com")
                .password("pass"),
            400,
            "Password must be between 6 and 25 characters"),

        // Password too long
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("testuser@gmail.com")
                .password("averylongpasswordexceedingthetwentyfivecharacterslimit"),
            400,
            "Password must be between 6 and 25 characters"),

        // Boundary test: Username at minimum length
        Arguments.of(
            new CreateAccountRequest()
                .username("usrnam")
                .email("testuser@gmail.com")
                .password("password123"),
            400,
            "Invalid credentials"), // Assuming other invalid criteria

        // Boundary test: Username at maximum length
        Arguments.of(
            new CreateAccountRequest()
                .username("thirtycharusernameexactlyyayy")
                .email("testuser@gmail.com")
                .password("password123"),
            400,
            "Invalid credentials"), // Assuming other invalid criteria

        // Boundary test: Email at minimum length
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("test@domain.com")
                .password("password123"),
            400,
            "Invalid credentials"), // Assuming other invalid criteria

        // Boundary test: Email at maximum length
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("averylongemailaddresswithmaximum50charactrs@ex.com")
                .password("password123"),
            400,
            "Invalid credentials"), // Assuming other invalid criteria

        // Boundary test: Password at minimum length
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("testuser@gmail.com")
                .password("passw6"),
            400,
            "Invalid credentials"), // Assuming other invalid criteria

        // Boundary test: Password at maximum length
        Arguments.of(
            new CreateAccountRequest()
                .username("validuser")
                .email("testuser@gmail.com")
                .password("thisis25characterslongxx"),
            400,
            "Invalid credentials") // Assuming other invalid criteria
        );
  }
}
