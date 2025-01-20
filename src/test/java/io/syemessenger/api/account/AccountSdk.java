package io.syemessenger.api.account;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

public interface AccountSdk extends AutoCloseable {

  Long createAccount(CreateAccountRequest request);

  AccountInfo updateAccount(UpdateAccountRequest request);

  void login(LoginAccountRequest request);

  AccountInfo getSessionAccount();

  PublicAccountInfo showAccount(Long id);

  @Override
  public void close();

}
