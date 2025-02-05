package io.syemessenger.api.account;

import static io.syemessenger.api.ErrorAssertions.assertError;
import static io.syemessenger.api.account.AccountAssertions.assertAccount;
import static io.syemessenger.api.account.AccountAssertions.login;
import static org.junit.jupiter.api.Assertions.fail;

import io.syemessenger.api.ClientSdk;
import io.syemessenger.environment.IntegrationEnvironmentExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(IntegrationEnvironmentExtension.class)
public class GetAccountIT {

  @Test
  void testGetAccountNotLoggedIn(ClientSdk clientSdk, AccountInfo accountInfo) {
    try {
      clientSdk.accountSdk().getAccount(accountInfo.id());
      fail("Expected exception");
    } catch (Exception ex) {
      assertError(ex, 401, "Not authenticated");
    }
  }

  @Test
  void testGetForeignAccount(ClientSdk clientSdk, AccountInfo accountInfo, AccountInfo anotherAccountInfo) {
    login(clientSdk, accountInfo);
    final var id = anotherAccountInfo.id();
    assertAccount(anotherAccountInfo, clientSdk.accountSdk().getAccount(id));
  }

  @Test
  void testGetOwnAccount(ClientSdk clientSdk, AccountInfo accountInfo) {
    login(clientSdk, accountInfo);
    assertAccount(accountInfo, clientSdk.accountSdk().getAccount(accountInfo.id()));
  }
}
