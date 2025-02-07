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
import org.junit.jupiter.params.provider.MethodSource;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class LoginIT {

  private static AccountInfo existingAccountInfo;

  @BeforeAll
  static void beforeAll() {
    existingAccountInfo = createAccount();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "testLoginFailedMethodSource")
  void testLoginFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      clientSdk.accountSdk().login(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, LoginAccountRequest request, int errorCode, String errorMessage) {
    @Override
    public String toString() {
      return test;
    }
  }

  private static Stream<?> testLoginFailedMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Login to non-existing account",
            new LoginAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        new FailedArgs(
            "Cannot provide both username and email",
            new LoginAccountRequest()
                .username(randomAlphanumeric(8, 65))
                .email("example@gmail.com")
                .password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        new FailedArgs(
            "Username and email are not provided",
            new LoginAccountRequest().password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        new FailedArgs(
            "Password not provided",
            new LoginAccountRequest().username(randomAlphanumeric(8, 65)),
            401,
            "Login failed"),
        new FailedArgs(
            "Invalid password",
            new LoginAccountRequest()
                .username(existingAccountInfo.username())
                .password(randomAlphanumeric(8, 65)),
            401,
            "Login failed"));
  }

  @Test
  void testLoginByUsername(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var accountId =
        clientSdk
            .accountSdk()
            .login(
                new LoginAccountRequest().username(accountInfo.username()).password("test12345"));

    assertEquals(accountInfo.id(), accountId, "accountId");
  }

  @Test
  void testLoginByEmail(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var accountId =
        clientSdk
            .accountSdk()
            .login(new LoginAccountRequest().email(accountInfo.email()).password("test12345"));

    assertEquals(accountInfo.id(), accountId, "accountId");
  }
}
