package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.assertAccount;
import static io.syemessenger.api.account.AccountAssertions.createAccount;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.environment.CloseHelper;
import io.syemessenger.environment.IntegrationEnvironment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetAccountIT {


  private ClientSdk clientSdk;
  private AccountSdk accountSdk;
  private AccountInfo existingAccountInfo;
  private AccountInfo anotherAccountInfo;

  @BeforeEach
  void beforeEach() {
    clientSdk = new ClientSdk();
    accountSdk = clientSdk.api(AccountSdk.class);
    existingAccountInfo = createAccount();
    anotherAccountInfo = createAccount();
  }

  @AfterEach
  void afterEach() {
    CloseHelper.close(clientSdk);
  }

  @Test
  void testGetAccountNotLoggedIn() {
    try {
      accountSdk.getAccount(existingAccountInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testGetForeignAccount() {
    accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    final var id = anotherAccountInfo.id();
    assertAccount(anotherAccountInfo, accountSdk.getAccount(id));
  }

  @Test
  void testGetOwnAccount() {
    final var accountId = accountSdk.login(
        new LoginAccountRequest().username(existingAccountInfo.username()).password("test12345"));
    assertAccount(existingAccountInfo, accountSdk.getAccount(accountId));
  }
}
