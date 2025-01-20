package io.syemessenger.api.account;

import io.syemessenger.api.AccountSession;

public class AccountService {

  public Long createAccount(CreateAccountRequest request) {
    return null;
  }

  public AccountInfo updateAccount(AccountSession session, UpdateAccountRequest request) {
    return null;
  }

  public Long login(AccountSession session, LoginAccountRequest request) {

    return null;
  }

  public AccountInfo getSessionAccount(AccountSession session) {
    return null;
  }

  public PublicAccountInfo showAccount(AccountSession session, Long id) {
    return null;
  }
}
