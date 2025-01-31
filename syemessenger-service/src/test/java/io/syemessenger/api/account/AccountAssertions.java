package io.syemessenger.api.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.testcontainers.shaded.org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

import io.syemessenger.api.ClientSdk;
import java.util.function.Consumer;

public class AccountAssertions {

  private AccountAssertions() {}

  public static AccountInfo createAccount() {
    return createAccount(null);
  }

  public static AccountInfo createAccount(Consumer<CreateAccountRequest> consumer) {
    try (ClientSdk clientSdk = new ClientSdk()) {
      final var username = randomAlphanumeric(8, 65);
      final var email =
          randomAlphanumeric(4) + "@" + randomAlphabetic(2, 10) + "." + randomAlphabetic(2, 10);
      final var password = "test12345";

      final var request =
          new CreateAccountRequest().username(username).email(email).password(password);

      if (consumer != null) {
        consumer.accept(request);
      }

      return clientSdk.api(AccountSdk.class).createAccount(request);
    }
  }

  public static void assertAccount(AccountInfo expected, AccountInfo actual) {
    assertEquals(expected.id(), actual.id(), "actual.id");
    assertEquals(expected.username(), actual.username(), "actual.username");
    assertEquals(expected.email(), actual.email(), "actual.email");
  }
}
