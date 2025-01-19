package io.syemessenger.api.account;

import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;

public interface AccountSdk {

  Long createAccount(CreateAccountRequest request) throws JsonProcessingException;

  AccountInfo updateAccount(UpdateAccountRequest request);

  void login(LoginAccountRequest request);

  AccountInfo getSessionAccount();

  PublicAccountInfo showAccount(Long id);

}
