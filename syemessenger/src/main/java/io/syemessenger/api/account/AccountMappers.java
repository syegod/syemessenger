package io.syemessenger.api.account;

import io.syemessenger.api.account.repository.Account;

public class AccountMappers {

  private AccountMappers() {}

  public static AccountInfo toAccountInfo(Account account) {
    return new AccountInfo()
        .id(account.id())
        .username(account.username())
        .email(account.email())
        .createdAt(account.createdAt())
        .updatedAt(account.updatedAt());
  }

  public static AccountViewInfo toAccountViewInfo(Account account) {
    return new AccountViewInfo().id(account.id()).username(account.username());
  }
}
