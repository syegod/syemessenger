package io.syemessenger.api.account;

public interface AccountSdk {

  Long createAccount(CreateAccountRequest request);

  AccountInfo updateAccount(UpdateAccountRequest request);

  void login(LoginAccountRequest request);

  AccountInfo getSessionAccount();

  PublicAccountInfo showAccount(Long id);

}
