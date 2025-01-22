package io.syemessenger.api.account;

import io.syemessenger.ServiceBootstrap;
import io.syemessenger.ServiceConfig;
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

  private static ServiceBootstrap serviceBootstrap;

  @BeforeAll
  static void beforeAll() {
    serviceBootstrap = new ServiceBootstrap(new ServiceConfig());
    serviceBootstrap.start();
  }

  @AfterAll
  static void afterAll() {
    if (serviceBootstrap != null) {
      serviceBootstrap.stop();
    }
  }

  @Test
  void testCreateAccount() {
    try (AccountSdk sdk = new AccountSdkImpl()) {
      String username = "Test123";
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
    } catch (Exception ex) {
      Assertions.assertInstanceOf(ServiceException.class, ex);
      final var serviceException = (ServiceException) ex;
      Assertions.assertEquals(errorCode, serviceException.errorCode());
      Assertions.assertEquals(errorMessage, serviceException.getMessage());
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
            new CreateAccountRequest().username("test").email("test@gmail.com"),
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
