package io.syemessenger;

import io.syemessenger.api.ServiceException;
import io.syemessenger.api.account.AccountSdk;
import io.syemessenger.api.account.CreateAccountRequest;
import java.util.stream.Stream;
import org.eclipse.jetty.websocket.core.util.InvokerUtils.Arg;
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
    AccountSdk sdk;

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

  @MethodSource(value = "failedAccountMethodSource")
  @ParameterizedTest
  void testCreateAccountFailed(CreateAccountRequest request, int errorCode, String errorMessage) {
    AccountSdk sdk;

    try {
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
            "Invalid credentials"));
  }
}
