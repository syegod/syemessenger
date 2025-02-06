package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
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
public class UpdateAccountIT {

  private static AccountInfo existingAccountInfo;

  @BeforeAll
  static void beforeAll() {
    existingAccountInfo = createAccount();
  }

  @Test
  void testUpdateAccountNotLoggedIn(ClientSdk clientSdk) {
    try {
      final var username = randomAlphanumeric(8, 65);
      final var email = "example@gmail.com";
      clientSdk
          .accountSdk()
          .updateAccount(new UpdateAccountRequest().username(username).email(email));
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource(value = "failedUpdateAccountMethodSource")
  void testUpdateAccountFailed(FailedArgs args, ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      login(clientSdk, accountInfo);
      clientSdk.accountSdk().updateAccount(args.request);
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, args.errorCode, args.errorMessage);
    }
  }

  private record FailedArgs(
      String test, UpdateAccountRequest request, int errorCode, String errorMessage) {}

  private static Stream<?> failedUpdateAccountMethodSource() {
    return Stream.of(
        new FailedArgs(
            "Username too long",
            new UpdateAccountRequest().username(randomAlphanumeric(80)),
            400,
            "Invalid: username"),
        new FailedArgs(
            "Username too short",
            new UpdateAccountRequest().username(randomAlphanumeric(7)),
            400,
            "Invalid: username"),
        new FailedArgs(
            "Wrong email type",
            new UpdateAccountRequest().email(randomAlphanumeric(8, 65)),
            400,
            "Invalid: email"),
        new FailedArgs(
            "Email too long",
            new UpdateAccountRequest().email(randomAlphanumeric(80)),
            400,
            "Invalid: email"),
        new FailedArgs(
            "Email too short",
            new UpdateAccountRequest().email(randomAlphanumeric(7)),
            400,
            "Invalid: email"),
        new FailedArgs(
            "Password too long",
            new UpdateAccountRequest().password(randomAlphanumeric(80)),
            400,
            "Invalid: password"),
        new FailedArgs(
            "Password too short",
            new UpdateAccountRequest().password(randomAlphanumeric(7)),
            400,
            "Invalid: password"),
        new FailedArgs(
            "Updating username to already existing",
            new UpdateAccountRequest().username(existingAccountInfo.username()),
            400,
            "Cannot update account: already exists"),
        new FailedArgs(
            "Updating email to already existing",
            new UpdateAccountRequest().email(existingAccountInfo.email()),
            400,
            "Cannot update account: already exists"));
  }

  @Test
  void testUpdateAccount(ClientSdk clientSdk, AccountInfo accountInfo) {
    final var accountSdk = clientSdk.accountSdk();
    final var username = randomAlphanumeric(8, 65);
    final var email = "example@gmail.com";

    login(clientSdk, accountInfo);

    final var updatedAccountInfo =
        accountSdk.updateAccount(new UpdateAccountRequest().username(username).email(email));

    assertEquals(
        accountInfo.id(), updatedAccountInfo.id(), "accountInfo.id: " + updatedAccountInfo.id());
    assertEquals(username, updatedAccountInfo.username(), "username");
    assertEquals(email, updatedAccountInfo.email(), "email");
  }
}
