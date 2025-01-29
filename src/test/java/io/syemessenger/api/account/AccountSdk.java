package io.syemessenger.api.account;

public interface AccountSdk extends AutoCloseable {

  AccountInfo createAccount(CreateAccountRequest request);

  AccountInfo updateAccount(UpdateAccountRequest request);

  Long login(LoginAccountRequest request);

  AccountInfo getSessionAccount();

  PublicAccountInfo showAccount(Long id);

  @Override
  void close();
}
